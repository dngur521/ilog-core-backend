package com.webkit640.ilog_core_backend.application.service;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MemoRequest;
import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.MemoDAO;
import com.webkit640.ilog_core_backend.domain.repository.MemoLogDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemberService memberService;
    private final MinutesService minutesService;
    private final MemoDAO memoDAO;
    private final MemoLogDAO memoLogDAO;
    @Transactional
    public List<Memo> createMemo(Long minutesId, MemoRequest.Create request, Long userId) {
        //---------------누가 썻는지 확인용 회원 조회--------------------
        Member member = memberService.getMember(userId);
        //----------------어디에 쓰는건지 확인용 회의록 조회--------------
        Minutes minutes = minutesService.getMinutes(minutesId, userId);
        //----------------메모 생성-----------------------------------
        Memo memo = creatingMemo(member,request,minutes);
        //-------------------------로그-----------------------------
        memoLogging(member.getId(),member.getEmail(),memo.getCreatedAt(),minutesId, ActionType.CREATE, request.getMemoType(),"정상 생성");
        //---------------------메모 리스트 리턴-----------------------
        return memoDAO.findAllByMinutes(minutes);
    }
    // 메모 조회
    public List<Memo> getMemo(Long minutesId, Long userId) {
        return minutesService.getMemo(minutesId, userId);
    }

    @Transactional
    public List<Memo> updateMemo(Long minutesId, MemoRequest.Update request, Long userId) {
        //------------- 인증된 메모 반환 ------------
        Memo memo = verifyMemo(minutesId, request.getUpdateId(), userId);
        //---------------------메모 수정------------------------------
        if(request.getContent() != null && !request.getContent().isBlank()){
            memo.setContent(request.getContent());
        }
        if(request.getMemoType() != null){
            memo.setMemoType(request.getMemoType());
        }
        if(request.getMemoType() != null && request.getContent() != null){
            memo.setUpdatedAt(LocalDateTime.now());
        }

        memoDAO.save(memo);

        //-------------------------로그-----------------------------
        String email = memberService.getMember(userId).getEmail();
        memoLogging(userId, email,memo.getUpdatedAt(),memo.getMinutes().getId(),ActionType.UPDATE,memo.getMemoType(),"정상 수정");
        //----------------------메모 리스트 리턴-----------------------
        return memoDAO.findAllByMinutes(memo.getMinutes());
    }
    @Transactional
    public void deleteMemo(Long minutesId, MemoRequest.Delete request, Long userId) {
        //------------- 인증된 메모 반환 ------------
        Memo memo = verifyMemo(minutesId, request.getDeleteId(), userId);
        //----------------------메모 삭제-------------------------------
        memoDAO.delete(memo);
        //-----------------------로그----------------------------------
        String email = memberService.getMember(userId).getEmail();
        memoLogging(userId,email, LocalDateTime.now(),minutesId,ActionType.DELETE,memo.getMemoType(),"정상 삭제");
    }
    //----------- 메모 생성 ------------------
    private Memo creatingMemo(Member member, MemoRequest.Create request, Minutes minutes){
        Memo memo = new Memo();
        memo.setMinutes(minutes);
        memo.setMember(member);
        memo.setContent(request.getContent());
        memo.setMemoType(request.getMemoType());
        memo.setCreatedAt(LocalDateTime.now());
        memo.setUpdatedAt(null);

        memoDAO.save(memo);
        return memo;
    }

    // ---------------- 메모 접근 가능한지 검증 ------------------------
    private Memo verifyMemo(Long minutesId, Long id, Long userId){
        //------------- 메모 찾기 ------------
        Memo memo = memoDAO.findById(id).orElseThrow(()->new CustomException(ErrorCode.MEMO_NOT_FOUND));
        //------------- 본인의 메모인지 확인 ----------------------
        identityVerification(memo, userId);
        //----------------- 이 메모가 회의록의 메모인지 확인--------------
        if(!memo.getMinutes().getId().equals(minutesId)){
            throw new CustomException(ErrorCode.MINUTES_NOT_MATCH);
        }
        return memo;
    }

    //------------ 누가 메모를 만들었는지 검증 ----------------
    private void identityVerification(Memo memo, Long ownerId){
        if(!memo.getMember().getId().equals(ownerId)){
            throw new CustomException(ErrorCode.UPDATE_DENIED);
        }
    }

    private void memoLogging(Long userId, String email, LocalDateTime createdAt, Long minutesId, ActionType actionType, MemoType memoType, String description){
        MemoLog memoLog = new MemoLog();
        memoLog.setUserId(userId);
        memoLog.setEmail(email);
        memoLog.setCreatedAt(createdAt);
        memoLog.setMinutesId(minutesId);
        memoLog.setStatus(actionType);
        memoLog.setMemoType(memoType);
        memoLog.setDescription(description);

        memoLogDAO.save(memoLog);
    }
}
