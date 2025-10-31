package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MemoRequest;
import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.MemoLog;
import com.webkit640.ilog_core_backend.domain.model.MemoType;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.repository.MemoDAO;
import com.webkit640.ilog_core_backend.domain.repository.MemoLogDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

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
        Memo memo = createMemo(member, request, minutes);
        //-------------------------로그-----------------------------
        memoLogging(member.getId(), member.getEmail(), memo.getCreatedAt(), minutesId, ActionType.CREATE, request.getMemoType(), "정상 생성");
        //---------------------메모 리스트 리턴-----------------------
        return memoDAO.findAllByMinutes(minutes);
    }

    // 메모 조회
    public List<Memo> getMemo(Long minutesId, Long userId) {
        Minutes minutes = minutesService.getMinutes(minutesId, userId);
        return memoDAO.findAllByMinutes(minutes);
    }

    @Transactional
    public List<Memo> updateMemo(Long minutesId, MemoRequest.Update request, CustomUserDetails user) {
        //------------어느 회의록의 어느 메모를 수정했는지 확인-------------
        Minutes minutes = minutesService.getMinutes(minutesId, user.getId());
        //---------------------메모 수정------------------------------
        Memo memo = memoDAO.findById(request.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMO_NOT_FOUND));

        if (request.getContent() != null && !request.getContent().isBlank()) {
            memo.setContent(request.getContent());
        }
        if (request.getMemoType() != null) {
            memo.setMemoType(request.getMemoType());
        }
        if (request.getMemoType() != null && request.getContent() != null) {
            memo.setUpdatedAt(LocalDateTime.now());
        }

        memoDAO.save(memo);
        //-------------------------로그-----------------------------
        memoLogging(user.getId(), user.getUsername(), memo.getUpdatedAt(), minutesId, ActionType.UPDATE, memo.getMemoType(), "정상 수정");
        //----------------------메모 리스트 리턴-----------------------
        return memoDAO.findAllByMinutes(minutes);
    }

    @Transactional
    public void deleteMemo(Long minutesId, MemoRequest.Delete request, CustomUserDetails user) {
        //------------- 어느 메모를 삭제했는지 확인 ------------
        Memo memo = memoDAO.findById(request.getDeleteId()).orElseThrow(() -> new CustomException(ErrorCode.MEMO_NOT_FOUND));
        //----------------------메모 삭제-------------------------------
        memoDAO.delete(memo);
        //-----------------------로그----------------------------------
        memoLogging(user.getId(), user.getUsername(), LocalDateTime.now(), minutesId, ActionType.DELETE, memo.getMemoType(), "정상 삭제");
    }

    private Memo createMemo(Member member, MemoRequest.Create request, Minutes minutes) {
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

    private void memoLogging(Long userId, String email, LocalDateTime createdAt, Long minutesId, ActionType actionType, MemoType memoType, String description) {
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
