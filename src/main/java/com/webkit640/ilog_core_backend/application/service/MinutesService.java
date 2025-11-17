package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.*;

import com.webkit640.ilog_core_backend.SummaryController;
import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.api.response.ParticipantResponse;
import com.webkit640.ilog_core_backend.application.mapper.MinutesMapper;
import com.webkit640.ilog_core_backend.application.mapper.ParticipantMapper;
import com.webkit640.ilog_core_backend.domain.event.MinutesDeletedEvent;
import com.webkit640.ilog_core_backend.domain.event.MinutesLogEvent;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.*;
import com.webkit640.ilog_core_backend.infrastructure.security.LinkTokenService;
import com.webkit640.ilog_core_backend.infrastructure.util.CreateMemberUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MinutesService {
    private final MinutesDAO minutesDAO;
    private final FolderDAO folderDAO;
    private final MinutesParticipantDAO minutesParticipantDAO;
    private final MinutesHistoryDAO minutesHistoryDAO;
    private final MemoHistoryDAO memoHistoryDAO;
    private final MemberService memberService;
    private final PermissionPropagationService permissionPropagationService;
    private final MinutesLockService minutesLockService;
    private final LinkTokenService linkTokenService;
    private final MinutesMapper minutesMapper;
    private final ParticipantMapper participantMapper;
    private final MemoDAO memoDAO;
    private final SummaryController summaryController;
    private final ApplicationEventPublisher eventPublisher;
    private final CreateMemberUtils createMemberUtils;
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
        minutes.setContent(request.getContent());

        //------------------------ ai에게 받아야함 ----------------------------
        SummaryController.SimpleSummaryRequest summaryRequest = new SummaryController.SimpleSummaryRequest();
        summaryRequest.setText(minutes.getContent());
        String summarize = summaryController.handleSimpleSummary(summaryRequest).getBody().getSummary();
        minutes.setSummary(summarize);
        LocalDateTime createdAt = LocalDateTime.now();
        minutes.setCreatedAt(createdAt);
        minutes.setUpdatedAt(null);
        if(MinutesType.MEETING.equals(request.getStatus())){
            minutes.setStatus(MinutesType.MEETING);
        }else{
            minutes.setStatus(MinutesType.NO_MEETING);
        }
        minutes.setFolder(folder);

        minutesDAO.save(minutes);

        List<MinutesParticipant> participants = new ArrayList<>(List.of());

        MinutesParticipant copy = new MinutesParticipant();
        copy.setMinutes(minutes);
        copy.setParticipant(owner);
        copy.setApproachedAt(createdAt);

        participants.add(copy);

        minutesParticipantDAO.saveAll(participants);
        minutes.setMinutesParticipants(participants);

        historyBackup(minutes);

        //--------------------------------로그-------------------------------
        eventPublisher.publishEvent(
                new MinutesLogEvent(this,ownerId,owner.getEmail(),createdAt,minutes.getTitle(),owner.getEmail(),ActionType.CREATE, "정상 생성")
        );
        return minutes;
    }
    
    //회의록 조회
    public Minutes getMinutes(Long minutesId, Long userId) {
        //--------------------- 회의록이 없으면 에러 ---------------------
        Minutes minutes = minutesDAO.findById(minutesId)
                .orElseThrow(()-> new CustomException(ErrorCode.MINUTES_NOT_FOUND));

        //---------- 권한 인증, 접근 못하면 돌려야한다. ----------------------
        participantVerification(minutes, userId);

        //----------------------- 회의록 리턴 -----------------------
        return minutes;
    }

    //회의록 상세 조회
    public MinutesResponse.FindContent getMinutesDetail(Long minutesId, Long participantId){
        Minutes minutes = getMinutes(minutesId, participantId);
        List<Memo> memos = getMemo(minutesId, participantId);

        setApproachedAt(minutes, participantId);

        return minutesMapper.toFindContent(minutes,memos);
    }

    //회의록 요약 조회
    public MinutesResponse.FindSummary getMinutesSummary(Long minutesId, Long participantId){
        Minutes minutes = getMinutes(minutesId, participantId);
        List<Memo> memos = getMemo(minutesId, participantId);

        setApproachedAt(minutes, participantId);

        return minutesMapper.toFindSummary(minutes,memos);
    }
    //회의록 수정(참가자면 가능)
    @Transactional
    public Minutes updateMinutes(Long minutesId, MinutesRequest.Update request, Long participantId) {

        //------------------ token이 들어있고 유효한지 검사 ----------------------------
        minutesLockService.validate(minutesId, request.getToken());

        Minutes minutes = getMinutes(minutesId, participantId);
        Member participant = memberService.getMember(participantId);

        if(MinutesType.MEETING.equals(minutes.getStatus())){
            throw new CustomException(ErrorCode.UPDATE_DENIED);
        }

        //---------------- 참가자면 수정 가능 -------------------
        identityParticipant(minutes, participant.getId());

        //------------------ history에 저장----------------------
        historyBackup(minutes);

        //수정
        if(request.getTitle() != null && !request.getTitle().isBlank()) {
            minutes.setTitle(request.getTitle());
        }
        if(request.getContent() != null && !request.getContent().isBlank()){
            minutes.setContent(request.getContent());
        }
        minutes.setUpdatedAt(LocalDateTime.now());
        minutesDAO.save(minutes);

//        //--------------------------------로그-------------------------------
//        eventPublisher.publishEvent(
//                new MinutesLogEvent(this,minutes.getFolder().getOwner().getId(),minutes.getFolder().getOwner().getEmail(),
//                        LocalDateTime.now(),minutes.getTitle(),participant.getEmail(),ActionType.UPDATE, "본문 수정")
//        );
        return minutes;
    }

    @Transactional
    public void deleteMinutes(Long minutesId, Long ownerId) {
        Minutes minutes = getMinutes(minutesId,ownerId);

        //----회의록 주인과 삭제할 대상이 같아야만 실행--------
        identityVerification(minutes.getFolder(), ownerId);
        //--------------------로그 남기기--------------------------------
        eventPublisher.publishEvent(new MinutesDeletedEvent(this, minutes));
        String email = memberService.getMember(ownerId).getEmail();
        //--------------------------------로그-------------------------------
        eventPublisher.publishEvent(
                new MinutesLogEvent(this,ownerId,email,LocalDateTime.now(),minutes.getTitle(),email,ActionType.DELETE, "정상 삭제")
        );
        //----------------- 회의록 삭제 -----------------------------
        minutesDAO.delete(minutes);
    }


    //회의록 재요약
    @Transactional
    public void summaryMinutes(Long minutesId, Long participantId){
        Minutes minutes = getMinutes(minutesId, participantId);
        Member participant = memberService.getMember(participantId);

        //------------------ history에 저장----------------------
        historyBackup(minutes);

        //-----------------------회의록 재요약 -------------------------------
        SummaryController.SimpleSummaryRequest summaryRequest = new SummaryController.SimpleSummaryRequest();
        summaryRequest.setText(minutes.getContent());
        String summarize = summaryController.handleSimpleSummary(summaryRequest).getBody().getSummary();
        minutes.setSummary(summarize);
        LocalDateTime updatedAt = LocalDateTime.now();
        minutes.setUpdatedAt(updatedAt);
        //------------------------ 수정 -------------------------------
        minutesDAO.save(minutes);

        //--------------------------------로그-------------------------------
        eventPublisher.publishEvent(
                new MinutesLogEvent(this,minutes.getFolder().getOwner().getId(),minutes.getFolder().getOwner().getEmail(),
                        updatedAt,minutes.getTitle(),participant.getEmail(),ActionType.UPDATE, "요약본 수정")
        );
    }

    //--------------------------- 히스토리 조회 -----------------------------
    public List<MinutesResponse.FindHistory> getMinutesHistory(Long minutesId, Long userId){
        //--------------------- 회의록이 없으면 에러 ---------------------
        Minutes minutes = minutesDAO.findById(minutesId)
                .orElseThrow(()-> new CustomException(ErrorCode.MINUTES_NOT_FOUND));

        //---------- 권한 인증, 접근 못하면 돌려야한다. ----------------------
        participantVerification(minutes, userId);

        //---------- 회의록 history DB에서 가져와서 return ----------------
        List<MinutesHistory> minutesHistories = minutesHistoryDAO.findAllByMinutesId(minutesId);
        List<MemoHistory> memoHistories = memoHistoryDAO.findAllByMinutesId(minutesId);

        return minutesMapper.toFindHistoryList(minutesHistories, memoHistories);
    }
    //--------------------------- 이전 버전으로 되돌리기 ---------------------
    @Transactional
    public MinutesResponse.FindHistory rollbackMinutes(Long historyId, Long minutesId, Long participantId) {
        Minutes minutes = getMinutes(minutesId, participantId);
        Member participant = memberService.getMember(participantId);

        //현재 버전 백업
        historyBackup(minutes);

        //--------------------- history에서 특정 버전 조회해서 되돌리기 ----------------------------
        MinutesHistory history = minutesHistoryDAO.findByMinutesIdAndHistoryId(minutesId, historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_FOUND));

        minutes.setTitle(history.getTitle());
        minutes.setContent(history.getContent());
        minutes.setSummary(history.getSummary());
        LocalDateTime updatedAt = LocalDateTime.now();
        minutes.setUpdatedAt(updatedAt);

        minutesDAO.save(minutes);

        memoDAO.deleteAllByMinutes_Id(minutesId);

        List<MemoHistory> memoHistories = memoHistoryDAO.findAllByMinutesIdAndMinutesHistoryId(minutesId, historyId);

        //----------------- 메모 롤백 -----------------------
        for (MemoHistory mh : memoHistories) {
            Memo memo = getMemo(mh, minutes);
            memoDAO.save(memo);
        }

        //--------------------------------로그-------------------------------
        eventPublisher.publishEvent(
                new MinutesLogEvent(this,minutes.getFolder().getOwner().getId(),minutes.getFolder().getOwner().getEmail(),
                        updatedAt,minutes.getTitle(),participant.getEmail(),ActionType.REVERT, "되돌리기")
        );
        return minutesMapper.toFindHistory(history, memoHistories);
    }

    //---------------------- 메모 롤백 ------------------------
    private static Memo getMemo(MemoHistory mh, Minutes minutes) {
        Memo memo = new Memo();
        //----------- setId 어짜피 auto_increment로 들어가는데 빼면 오류나고 넣으면 오류가 해결됨 -----------
        //----------- AI피셜 setId(null) 넣으면 insert로 판단한다고 함 ----------------------------------
        memo.setId(null);
        memo.setMinutes(minutes);
        memo.setLocalId(mh.getLocalId());
        memo.setMember(mh.getMember());
        memo.setMemoType(mh.getMemoType());
        memo.setContent(mh.getContent());
        memo.setStartIndex(mh.getStartIndex());
        memo.setEndIndex(mh.getEndIndex());
        memo.setPositionContent(mh.getPositionContent());
        memo.setCreatedAt(mh.getCreatedAt());
        memo.setUpdatedAt(mh.getUpdatedAt());
        return memo;
    }

    //------------------------------------권한 관리-----------------------------------------
    // 참가자 추가
    public List<MinutesParticipant> createParticipant(Long minutesId, ParticipantRequest.Create request, Long ownerId) {
        Minutes minutes = getMinutes(minutesId, ownerId);

        Member createMember = createMemberUtils.getCreateMember(request);

        //---------------본인 인증-----------------------
        identityVerification(minutes.getFolder(), ownerId);
        //-------------------request에 맴버가 들어왔는지 확인-------------------
        folderParticipantRequestIsNull(createMember);
        //-------------------이미 참여되어 있는지 검증-------------------
        alreadyParticipant(minutes, createMember);
        //-------------------참여자 추가-------------------
        permissionPropagationService.grantToMinutes(minutesId,createMember.getId());
        //--------------수정된 회원 리스트 리턴-----------
        return minutesParticipantDAO.findByMinutes(minutes);
    }

    @Transactional
    public void joinByInvite(Long minutesId, Long userId){
        Minutes minutes = minutesDAO.findById(minutesId)
                .orElseThrow(()-> new CustomException(ErrorCode.MINUTES_NOT_FOUND));
        Member member = memberService.getMember(userId);

        var existing = minutesParticipantDAO.findByMinutesAndParticipant(minutes,member);
        if(existing.isPresent()) return;

        permissionPropagationService.grantToMinutes(minutesId, userId);
    }

    //참가자 조회
    public ParticipantResponse.DetailLink<ParticipantResponse.MinutesParticipant> getParticipant(
            Long minutesId, Long ownerId)
    {
        Minutes minutes = getMinutes(minutesId, ownerId);
        participantVerification(minutes, ownerId);
        var participant = minutesParticipantDAO.findByMinutes(minutes);
        String link = linkTokenService.createLink("MINUTES  ", minutesId);

        return participantMapper.toMinutesDetailLink(participant,link);
    }
    @Transactional
    // 참가자 퇴출
    public List<MinutesParticipant> deleteParticipant(Long minutesId, ParticipantRequest.Delete request, Long ownerId) {
        Minutes minutes = getMinutes(minutesId, ownerId);
        Member deleteMember = memberService.getMember(request.getDeleteMemberId());

        //---------------본인 인증-----------------------
        identityVerification(minutes.getFolder(), ownerId);
        //-------------삭제할 회원 확인-------------------
        folderParticipantRequestIsNull(deleteMember);
        //--------------회의록 주인 본인 삭제 X-----------------
        if(minutes.getFolder().getOwner().getId().equals(request.getDeleteMemberId())){
            throw new CustomException(ErrorCode.PERMISSION_SELF_DELETE_DENIED);
        }
        //----------------삭제 -------------------------
        permissionPropagationService.removeGrantToMinutes(minutesId, deleteMember.getId());
        //----------------------로그--------------------------
        String email = memberService.getMember(ownerId).getEmail();
        permissionPropagationService.participantLogging(ownerId,email,LocalDateTime.now(),deleteMember.getEmail(), ParticipantType.MINUTES,ActionType.DELETE,"참여자 삭제");
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
        boolean hasAccess = minutes.getFolder().getFolderParticipants().stream().anyMatch(
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

    //history 백업
    private void historyBackup(Minutes minutes){
        Long latestVersion = minutesHistoryDAO.findMaxHistoryIdByMinutesId(minutes.getId()).orElse(0L);

        MinutesHistory history = new MinutesHistory();
        history.setMinutesId(minutes.getId());
        history.setHistoryId(latestVersion + 1);
        history.setTitle(minutes.getTitle());
        history.setContent(minutes.getContent());
        history.setSummary(minutes.getSummary());
        history.setCreatedAt(minutes.getCreatedAt());
        history.setUpdatedAt(LocalDateTime.now());
        history.setStatus(minutes.getStatus());
        history.setFolder(minutes.getFolder());

        minutesHistoryDAO.save(history);

        List<Memo> memos = memoDAO.findAllByMinutes(minutes);
        for (Memo memo : memos) {
            MemoHistory mh = getMemoHistory(memo, history);
            history.getMemoHistories().add(mh);
        }
    }

    //memohistory 남기기
    private static MemoHistory getMemoHistory(Memo memo, MinutesHistory history) {
        MemoHistory mh = new MemoHistory();
        mh.setLocalId(memo.getLocalId());
        mh.setMinutesId(memo.getMinutes().getId());
        mh.setMinutesHistory(history);  // <-- 연결 포인트
        mh.setMember(memo.getMember());
        mh.setMemoType(memo.getMemoType());
        mh.setContent(memo.getContent());
        mh.setCreatedAt(memo.getCreatedAt());
        mh.setUpdatedAt(memo.getUpdatedAt());
        mh.setStartIndex(memo.getStartIndex());
        mh.setEndIndex(memo.getEndIndex());
        mh.setPositionContent(memo.getPositionContent());
        return mh;
    }

    // 참여 일시 남기기
    private void setApproachedAt(Minutes minutes, Long participantId) {
        Member participant = memberService.getMember(participantId);

        // MinutesParticipant가 없는 경우: 폴더 참가자인 경우도 있으므로 패스
        Optional<MinutesParticipant> optionalMp =
                minutesParticipantDAO.findByMinutesAndParticipant(minutes, participant);

        if (optionalMp.isEmpty()) {
            // 폴더 참가자라 MinutesParticipant 엔티티가 없을 수 있음 → 그냥 패스
            return;
        }

        // MinutesParticipant가 있는 경우만 approachedAt 업데이트
        MinutesParticipant minutesParticipant = optionalMp.get();
        minutesParticipant.setApproachedAt(LocalDateTime.now());
        minutesParticipantDAO.save(minutesParticipant);
    }

}
