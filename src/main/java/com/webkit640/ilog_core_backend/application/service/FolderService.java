package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.FolderRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.api.response.FolderResponse;
import com.webkit640.ilog_core_backend.application.mapper.FolderMapper;
import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.FolderLog;
import com.webkit640.ilog_core_backend.domain.model.FolderParticipant;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.repository.FolderDAO;
import com.webkit640.ilog_core_backend.domain.repository.FolderLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.FolderParticipantDAO;
import com.webkit640.ilog_core_backend.domain.repository.MinutesDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderDAO folderDAO;
    private final MemberService memberService;
    private final MinutesDAO minutesDAO;
    private final FolderParticipantDAO folderParticipantDAO;
    private final FolderMapper folderMapper;
    private final FolderLogDAO folderLogDAO;
    private final PermissionPropagationService permissionPropagationService;

    //폴더 생성
    @Transactional
    public Folder createFolder(Long folderId, FolderRequest.Create request, Long ownerId) {
        Folder folder = getFolder(folderId);
        Member owner = memberService.getMember(ownerId);

        //-------------------본인 인증-------------------
        identityVerification(folder, ownerId);
        //-------------------폴더 생성-------------------
        Folder newFolder = new Folder();

        //부모의 참여자 리스트를 참조하기에 참여자 변경 시 부모/자식 폴더가 같이 바뀜
        List<FolderParticipant> clonedParticipants = folder.getFolderParticipants().stream()
                        .map(fp -> {
                            FolderParticipant copy = new FolderParticipant();
                            copy.setFolder(newFolder);
                            copy.setParticipant(fp.getParticipant());
                            return copy;
                        }).toList();

        newFolder.setParentFolder(folder);
        //-------------------동기화 추가-------------------
        folder.getChildFolders().add(newFolder);
        newFolder.setOwner(owner);
        newFolder.setFolderParticipants(clonedParticipants);
        newFolder.setFolderName(request.getFolderName());
        newFolder.setCreatedAt(LocalDateTime.now());
        newFolder.setUpdatedAt(null);

        if(request.getImageUrl() != null && !request.getImageUrl().isBlank()){
            newFolder.setImageUrl(request.getImageUrl());
        }

        folderDAO.save(newFolder);

        //참여일시
        FolderParticipant folderParticipant = folderParticipantDAO.findByFolderAndParticipant(folder,owner)
                .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        folderParticipant.setApproachedAt(LocalDateTime.now());

        //------------------------ 로그 -------------------------
        folderLogging(owner.getId(),owner.getEmail(),folder.getCreatedAt(),folder.getId(), ActionType.CREATE,"정상 생성");

        return newFolder;
    }
    
    //폴더 조회
    public FolderResponse.Find getFolderDetail(Long folderId, Long userId){
        Folder folder = getFolder(folderId);
        Member participant = memberService.getMember(userId);
        //-------------------참여자 인증-------------------
        participantVerification(folder, userId);
        //-------------------폴더 조회-------------------
        List<Folder> childFolders = folderDAO.findByParentFolder(folder);
        List<Minutes> minutesList = minutesDAO.findByFolder(folder);

        //참여일시
        FolderParticipant folderParticipant = folderParticipantDAO.findByFolderAndParticipant(folder,participant)
                .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        folderParticipant.setApproachedAt(LocalDateTime.now());

        return folderMapper.toFind(folder, childFolders, minutesList);
    }

    //폴더 수정
    @Transactional
    public Folder updateFolder(Long folderId, FolderRequest.Update request, Long ownerId) {
        Folder folder = getFolder(folderId);
        Member owner = memberService.getMember(ownerId);
        //-------------------본인 인증-------------------
        identityVerification(folder, owner.getId());
        //-------------------폴더 수정-------------------
        if(request.getFolderName() != null && !request.getFolderName().isBlank()){
            folder.setFolderName(request.getFolderName());
        }
        if(request.getImageUrl() != null && !request.getImageUrl().isBlank()){
            folder.setImageUrl(request.getImageUrl());
        }
        //변경 사항이 있을 때만
        if(request.getFolderName() != null || request.getImageUrl() != null){
            folder.setUpdatedAt(LocalDateTime.now());

            folderDAO.save(folder);

            //참여일시
            FolderParticipant folderParticipant = folderParticipantDAO.findByFolderAndParticipant(folder,owner)
                    .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
            folderParticipant.setApproachedAt(LocalDateTime.now());

            //-------------------로그 남기기------------------
            folderLogging(owner.getId(), owner.getEmail(),folder.getUpdatedAt(),folder.getId(),ActionType.UPDATE,"정상 수정");
        }

        return folder;
    }

    //폴더 삭제
    @Transactional
    public Folder deleteFolder(Long folderId, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);

        //-------------------본인 인증-------------------
        identityVerification(folder, owner.getId());
        //-------------------재귀적으로 삭제-------------------
        folderDAO.delete(folder);
        //------------------- 로그 -------------------
        folderLogging(owner.getId(), owner.getUsername(),LocalDateTime.now(),folder.getId(),ActionType.DELETE,"정상 삭제");
        
        return folder;
    }

    //폴더가 있는지 확인
    private Folder getFolder(Long folderId){
        return folderDAO.findByIdWithParticipantsAndChildren(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
    }
    //주인인지 확인
    private void identityVerification(Folder folder,Long ownerId){
        if(!folder.getOwner().getId().equals(ownerId)){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
    //참여한 사람 인지 확인 => 접근 권한 확인
    private void participantVerification(Folder folder, Long userId){
        boolean hasAccess = folder.getFolderParticipants().stream().anyMatch(
                fp->fp.getParticipant().getId().equals(userId));

        if(!hasAccess){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    //-----------------------------------------권한---------------------------------------
    @Transactional
    public List<FolderParticipant> createParticipant(Long folderId, ParticipantRequest.Create request, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);
        Member createMember = memberService.getMember(request.getCreateMemberId());

        //---------------본인 인증-----------------------
        identityVerification(folder, owner.getId());
        //-------------------request에 맴버가 들어왔는지 확인-------------------
        folderParticipantRequestIsNull(createMember);
        //-------------------이미 참여되어 있는지 검증-------------------
        alreadyParticipant(folder, createMember);
        //-------------------참여자 추가-------------------
        permissionPropagationService.grantToFolders(folderId, request.getCreateMemberId());
        //--------------수정된 회원 리스트 리턴-------------
        return folderParticipantDAO.findByFolder(folder);
    }
    //참여자 조회
    public List<FolderParticipant> getParticipant(Long folderId, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);
        identityVerification(folder, owner.getId());
        return folderParticipantDAO.findByFolder(folder);
    }
    //참여자 삭제
    @Transactional
    public List<FolderParticipant> deleteParticipant(Long folderId, ParticipantRequest.Delete request, CustomUserDetails owner) {
        Folder folder = getFolder(folderId);
        Member deleteMember = memberService.getMember(request.getDeleteMemberId());

        //---------------본인 인증-----------------------
        identityVerification(folder, owner.getId());
        //-------------삭제할 회원 확인-------------------
        folderParticipantRequestIsNull(deleteMember);
        //----------------삭제-------------------------
        permissionPropagationService.removeToFoldersAndMinutes(folderId, request.getDeleteMemberId());
        //--------------수정된 회원 리스트 리턴-----------
        return folderParticipantDAO.findByFolder(folder);
    }

    //회원이 잘 들어왔는지 확인
    private void folderParticipantRequestIsNull(Member member){
        if(member == null){
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
    //이미 등록되었는지 확인
    private void alreadyParticipant(Folder folder, Member user){
        boolean exists = folderParticipantDAO.existsByFolderAndParticipant(folder, user);
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPANT);
        }
    }

    private void folderLogging(Long userId, String email, LocalDateTime createdAt, Long folderId, ActionType status, String description){
        FolderLog folderLog = new FolderLog();
        folderLog.setUserId(userId);
        folderLog.setEmail(email);
        folderLog.setCreatedAt(createdAt);
        folderLog.setFolderId(folderId);
        folderLog.setStatus(status);
        folderLog.setDescription(description);

        folderLogDAO.save(folderLog);
    }
}
