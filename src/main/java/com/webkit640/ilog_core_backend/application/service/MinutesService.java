package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.application.mapper.MinutesMapper;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MinutesService {
    private final MinutesDAO minutesDAO;
    private final FolderDAO folderDAO;
    private final MinutesLogDAO minutesLogDAO;
    private final MinutesParticipantDAO minutesParticipantDAO;
    private final MemberService memberService;
    private final PermissionPropagationService permissionPropagationService;
    private final MinutesMapper minutesMapper;
    private final MemoDAO memoDAO;
    //회의록 생성(주인만)
    @Transactional
    public Minutes createMinutes(Long folderId, MinutesRequest.Create request, Long ownerId) {
        //회의록 생성 및 값 넣기
        Folder folder = folderDAO.findById(folderId).orElseThrow(()-> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
        Member owner = memberService.getMember(ownerId);
        //owner만 접근 가능
        identityVerification(folder, owner.getId());

        Minutes minutes = new Minutes();
        minutes.setTitle(request.getTitle());

        //화상회의로 생긴 결과인지 아닌지 판별
        if(request.getStatus().equals(MinutesType.MEETING)) {
            minutes.setContent("본문 넣기, 화상회의의 결과");
        }else{
            minutes.setContent(request.getContent());
        }

        //------------------------ ai에게 받아야함 ----------------------------
        minutes.setSummary("Ai가 요약한 값을 넣기");
        minutes.setCreatedAt(LocalDateTime.now());
        minutes.setUpdatedAt(null);
        minutes.setStatus(request.getStatus());
        minutes.setFolder(folder);

        minutesDAO.save(minutes);

        //--------------------------folder에게 particiant를 물려받음----------------
        List<MinutesParticipant> clonedParticipants = folder.getFolderParticipants().stream()
                .map(mp->{
                    MinutesParticipant copy = new MinutesParticipant();
                    copy.setMinutes(minutes);
                    copy.setParticipant(mp.getParticipant());
                    return copy;
                }).toList();
        minutesParticipantDAO.saveAll(clonedParticipants);
        minutes.setMinutesParticipants(clonedParticipants);

        //참여일시
        MinutesParticipant minutesParticipant = minutesParticipantDAO.findByMinutesAndParticipant(minutes,owner)
                .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        minutesParticipant.setApproachedAt(LocalDateTime.now());

        //--------------------------------로그-------------------------------
        minutesLogging(owner.getId(),owner.getEmail(),minutes.getCreatedAt(),minutes.getId(),ActionType.CREATE,"정상 생성");

        return minutes;
    }
    
    //회의록 조회
    public Minutes getMinutes(Long minutesId, Long userId) {
        //--------------------- 회의록이 없으면 에러 ---------------------
        Minutes minutes = minutesDAO.findById(minutesId)
                .orElseThrow(()-> new CustomException(ErrorCode.MINUTES_NOT_FOUND));
        Member participant = memberService.getMember(userId);

        //---------- 권한 인증, 접근 못하면 돌려야한다. ----------------------
        participantVerification(minutes, userId);

        //---------------------------- 참여일시 남기기 ----------------------------
        MinutesParticipant minutesParticipant = minutesParticipantDAO.findByMinutesAndParticipant(minutes,participant)
                .orElseThrow(()-> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        minutesParticipant.setApproachedAt(LocalDateTime.now());

        //----------------------- 회의록 리턴 -----------------------
        return minutes;
    }

    //회의록 상세 조회
    public MinutesResponse.FindContent getMinutesDetail(Long minutesId, Long userId){
        Minutes minutes = getMinutes(minutesId, userId);
        List<Memo> memos = getMemo(minutesId, userId);
        return minutesMapper.toFindContent(minutes,memos);
    }

    //회의록 요약 조회
    public MinutesResponse.FindSummary getMinutesSummary(Long minutesId, Long userId){
        Minutes minutes = getMinutes(minutesId, userId);
        List<Memo> memos = getMemo(minutesId, userId);
        return minutesMapper.toFindSummary(minutes,memos);
    }
    //회의록 수정(주인만)
    @Transactional
    public Minutes updateMinutes(Long minutesId, MinutesRequest.Update request, Long ownerId) {
        Minutes minutes = getMinutes(minutesId, ownerId);
        Member owner = memberService.getMember(ownerId);

        if(minutes.getStatus().equals(MinutesType.MEETING)){
            throw new CustomException(ErrorCode.UPDATE_DENIED);
        }

        //owner만 접근 가능
        identityVerification(minutes.getFolder(), owner.getId());

        //수정
        if(request.getTitle() != null && !request.getTitle().isBlank()) {
            minutes.setTitle(request.getTitle());
        }
        if(request.getContent() != null && !request.getContent().isBlank()){
            minutes.setContent(request.getContent());
        }

        //-----------------------회의록 재요약 해야함-------------------------------
        //minutes.setSummary(요약본);
        minutes.setUpdatedAt(LocalDateTime.now());

        minutesDAO.save(minutes);

        //--------------------------------로그-------------------------------
        minutesLogging(owner.getId(),owner.getEmail(),minutes.getUpdatedAt(),minutes.getId(),ActionType.UPDATE,"정상 수정");

        return minutes;
    }


    @Transactional
    public void deleteMinutes(Long minutesId, CustomUserDetails owner) {
        Minutes minutes = getMinutes(minutesId,owner.getId());

        //----회의록 주인과 삭제할 대상이 같아야만 실행--------
        identityVerification(minutes.getFolder(), owner.getId());

        //----------------- 회의록 삭제 -----------------------------
        minutesDAO.delete(minutes);

        //--------------------로그 남기기--------------------------------
        minutesLogging(owner.getId(),owner.getUsername(),LocalDateTime.now(),minutes.getId(),ActionType.DELETE,"정상 삭제");
    }

    //------------------------------------권한 관리-----------------------------------------
    // 참가자 추가
    public List<MinutesParticipant> createParticipant(Long minutesId, ParticipantRequest.Create request, CustomUserDetails owner) {
        Minutes minutes = getMinutes(minutesId, owner.getId());
        Member createMember = memberService.getMember(request.getCreateMemberId());
        //---------------본인 인증-----------------------
        identityVerification(minutes.getFolder(), owner.getId());
        //-------------------request에 맴버가 들어왔는지 확인-------------------
        folderParticipantRequestIsNull(createMember);
        //-------------------이미 참여되어 있는지 검증-------------------
        alreadyParticipant(minutes, createMember);
        //-------------------참여자 추가-------------------
        permissionPropagationService.grantToMinutes(minutesId,request.getCreateMemberId());
        //--------------수정된 회원 리스트 리턴-----------
        return minutesParticipantDAO.findByMinutes(minutes);
    }

    //참가자 조회
    public List<MinutesParticipant> getParticipant(Long minutesId, CustomUserDetails owner) {
        Minutes minutes = getMinutes(minutesId, owner.getId());

        identityParticipant(minutes, owner.getId());
        return minutesParticipantDAO.findByMinutes(minutes);
    }
    @Transactional
    // 참가자 퇴출
    public List<MinutesParticipant> deleteParticipant(Long minutesId, ParticipantRequest.Delete request, CustomUserDetails owner) {
        Minutes minutes = getMinutes(minutesId, owner.getId());
        Member deleteMember = memberService.getMember(request.getDeleteMemberId());

        //---------------본인 인증-----------------------
        identityVerification(minutes.getFolder(), owner.getId());
        //-------------삭제할 회원 확인-------------------
        folderParticipantRequestIsNull(deleteMember);
        //--------------회의록 주인 본인 삭제 X-----------------
        if(minutes.getFolder().getOwner().getId().equals(owner.getId())){
            throw new CustomException(ErrorCode.PERMISSION_SELF_DELETE_DENIED);
        }
        //----------------삭제-------------------------
        minutesParticipantDAO.deleteByMinutesAndParticipant(minutes, deleteMember);
        //----------------------로그--------------------------
        permissionPropagationService.participantLogging(owner.getId(),owner.getUsername(),LocalDateTime.now(),deleteMember.getEmail(), ParticipantType.MINUTES,ActionType.DELETE,"참여자 삭제");
        //--------------수정된 회원 리스트 리턴-----------
        return minutesParticipantDAO.findByMinutes(minutes);
    }

    // 폴더의 주인인지 검증
    private void identityVerification(Folder folder, Long ownerId){
        if(!folder.getOwner().getId().equals(ownerId)){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    //참여자에 포함되어있다면 조회가능
    private void identityParticipant(Minutes minutes, Long userId) {
        boolean isParticipant = minutes.getMinutesParticipants().stream()
                .anyMatch(mp ->
                        mp.getParticipant().getId().equals(userId));
        if (!isParticipant) {
            throw new CustomException(ErrorCode.VIEW_DENIED);
        }
    }
    //회의록 접근 권한이 있는지 확인
    private void participantVerification(Minutes minutes, Long userId){
        boolean hasAccess = minutes.getMinutesParticipants().stream().anyMatch(
                fp->fp.getParticipant().getId().equals(userId));

        if(!hasAccess){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
    //회원이 잘 들어왔는지 확인
    private void folderParticipantRequestIsNull(Member member){
        if(member == null){
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
    //이미 등록되었는지 확인
    private void alreadyParticipant(Minutes minutes, Member user){
        boolean exists = minutesParticipantDAO.existsByMinutesAndParticipant(minutes, user);
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPANT);
        }
    }

    //순환 떄문에 만든 getMemo
    public List<Memo> getMemo(Long minutesId, Long userId) {
        Minutes minutes = getMinutes(minutesId, userId);
        return memoDAO.findAllByMinutes(minutes);
    }

    //회의록 로그 남기기
    private void minutesLogging(Long userId, String email, LocalDateTime createdAt, Long minutesId, ActionType status, String description){
        MinutesLog minutesLog = new MinutesLog();
        minutesLog.setUserId(userId);
        minutesLog.setEmail(email);
        minutesLog.setCreatedAt(createdAt);
        minutesLog.setMinutesId(minutesId);
        minutesLog.setStatus(status);
        minutesLog.setDescription(description);

        minutesLogDAO.save(minutesLog);
    }


}
