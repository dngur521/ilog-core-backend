package com.webkit640.ilog_core_backend.application.service;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//쉽지 않음
@Service
@RequiredArgsConstructor
public class PermissionPropagationService {
    private final FolderDAO folderDAO;
    private final MinutesDAO minutesDAO;
    private final FolderParticipantDAO folderParticipantDAO;
    private final MinutesParticipantDAO minutesParticipantDAO;
    private final MemberService memberService;
    private final ParticipantLogDAO participantLogDAO;

    //상향 전파: 회의록에 추가 -> 상위 폴더/조상 폴더에도 권한 보장
    @Transactional
    public void grantToMinutes(Long minutesId, Long memberId){
        Minutes minutes = minutesDAO.findById(minutesId)
                .orElseThrow(()-> new CustomException(ErrorCode.MINUTES_NOT_FOUND));
        Member member = memberService.getMember(memberId);

        //--------------회의록에 참가자 없으면 추가---------------
        addMinutesParticipantIfAbsent(minutes,member);
       //---------------상위 폴더로 bubble-up---------------
        bubbleUpToAncestors(minutes.getFolder(),member);
    }
    
    // 하향 전파 : 폴더에 추가 -> 모든 하위 폴더에도 권한 보장
    @Transactional
    public void grantToFolders(Long folderId, Long memberId){
        Folder folder = folderDAO.findById(folderId)
                .orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
        Member member = memberService.getMember(memberId);

        //----------------현재 폴더에 권한 추가---------------------------
        addFolderParticipantIfAbsent(folder, member);
        //------------------ 하위 폴더에도 권한 추가---------------------
        for (Folder child : collectDescendants(folder)){
            addFolderParticipantIfAbsent(child,member);
        }
    }

    // 상위 -> 하위 전체 삭제 : 폴더에서 제거 -> 모든 하위 폴더/회의록에서도 제거
    @Transactional
    public void removeToFoldersAndMinutes(Long folderId, Long memberId, Long ownerId){
        Folder folder = folderDAO.findByIdWithOwnerAndParticipants(folderId)
                .orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
        Member member = memberService.getMember(memberId);

        // 현재 폴더의 회의록에서 참가자 삭제
        for(Minutes m : minutesDAO.findByFolder(folder)){
            removeMinutesParticipantIfPresent(m,member, ownerId);
        }
        // 하위 폴더 회의록에서 삭제 + 폴더에서 삭제
        List<Folder> descendants = collectDescendants(folder);
        for (Folder f : descendants){
            // 하위 폴더의 회의록 참가자 삭제
            for (Minutes m : minutesDAO.findByFolder(f)){
                removeMinutesParticipantIfPresent(m,member, ownerId);
            }

            // 하위 폴더 참가자 삭제
            removeFolderParticipantIfPresent(f, member,ownerId);
        }
        //현재 폴더 삭제
        removeFolderParticipantIfPresent(folder, member, ownerId);
    }

    //------------------------유틸--------------------------------

    //-------------상위 폴더로 bubble-up---------------
    private void bubbleUpToAncestors(Folder folder, Member member){
        Folder cursor = folder;
        while(cursor != null){
            addFolderParticipantIfAbsent(cursor,member);
            cursor = cursor.getParentFolder();
        }
    }

    // --------------------회의록에 참가자 없으면 추가---------------------
    private void addMinutesParticipantIfAbsent(Minutes minutes, Member member){
        if(!minutesParticipantDAO.existsByMinutesAndParticipant(minutes,member)){
            MinutesParticipant mp = new MinutesParticipant();
            mp.setMinutes(minutes);
            mp.setParticipant(member);
            minutesParticipantDAO.save(mp);

            //----------------------로그--------------------------
            Member owner = minutes.getFolder().getOwner();
            participantLogging(owner.getId(),owner.getEmail(),LocalDateTime.now(),member.getEmail(), ParticipantType.MINUTES, ActionType.CREATE,"참여자 추가");
        }
    }
    //--------------------폴더에 참여자가 있는지 확인 후, 없으면 추가----------
    private void addFolderParticipantIfAbsent(Folder folder, Member member){
        if(!folderParticipantDAO.existsByFolderAndParticipant(folder,member)){
            FolderParticipant fp = new FolderParticipant();
            fp.setFolder(folder);
            fp.setParticipant(member);
            fp.setApproachedAt(LocalDateTime.now());
            folderParticipantDAO.save(fp);
            //----------------------로그--------------------------
            Member owner = folder.getOwner();
            participantLogging(owner.getId(),owner.getEmail(),LocalDateTime.now(),member.getEmail(), ParticipantType.FOLDER,ActionType.CREATE,"참여자 추가");
        }
    }

    //폴더에 참가자가 있으면 제거
    private void removeFolderParticipantIfPresent(Folder folder, Member member, Long ownerId){
        Member owner = folder.getOwner();
        //--------------폴더 주인 본인 삭제 X-----------------
        if(member.getId().equals(ownerId)){
            throw new CustomException(ErrorCode.PERMISSION_SELF_DELETE_DENIED);
        }
        folderParticipantDAO.deleteByFolderAndParticipant(folder, member);
        //----------------------로그--------------------------
        participantLogging(owner.getId(),owner.getEmail(),LocalDateTime.now(),member.getEmail(), ParticipantType.FOLDER,ActionType.DELETE,"참여자 퇴출");

    }

    //파일에 참가자가 있으면 제거
    private void removeMinutesParticipantIfPresent(Minutes minutes, Member member,Long ownerId){
        //----------------------로그--------------------------
        Member owner = minutes.getFolder().getOwner();
        //--------------회의록 주인 본인 삭제 X-----------------
        if(member.getId().equals(ownerId)){
            throw new CustomException(ErrorCode.PERMISSION_SELF_DELETE_DENIED);
        }
        participantLogging(owner.getId(),owner.getEmail(),LocalDateTime.now(),member.getEmail(), ParticipantType.MINUTES,ActionType.DELETE,"참여자 퇴출");
        minutesParticipantDAO.deleteByMinutesAndParticipant(minutes, member);
    }

    //------------------------ 모든 자식 가져온다 -----------------------
    private List<Folder> collectDescendants(Folder root){
        return folderDAO.findAllChildren(root.getId());
    }

    //참여자 로그 남기기
    public void participantLogging(Long userId, String email, LocalDateTime createdAt, String folderParticipantEmail, ParticipantType participantType, ActionType status, String description){
        ParticipantLog minutesParticipantLog = new ParticipantLog();
        minutesParticipantLog.setUserId(userId);
        minutesParticipantLog.setEmail(email);
        minutesParticipantLog.setCreatedAt(createdAt);
        minutesParticipantLog.setParticipantEmail(folderParticipantEmail);
        minutesParticipantLog.setParticipantType(participantType);
        minutesParticipantLog.setStatus(status);
        minutesParticipantLog.setDescription(description);

        participantLogDAO.save(minutesParticipantLog);
    }
}

