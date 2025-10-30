package com.webkit640.ilog_core_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.MemoRequest;
import com.webkit640.ilog_core_backend.api.response.MemoResponse;
import com.webkit640.ilog_core_backend.application.mapper.MemoMapper;
import com.webkit640.ilog_core_backend.application.service.MemoService;
import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/minutes")
public class MemoController {
    private final MemoMapper mapper;
    private final MemoService memoService;
    //생성 (회의록 ID, req, userId)
    @PostMapping("/{minutesId}/memos")
    public ResponseEntity<MemoResponse.Detail> createMemo(
            @PathVariable("minutesId") Long minutesId,
            @RequestBody MemoRequest.Create request,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getId();
        List<Memo> memos = memoService.createMemo(minutesId, request, userId);
        return ResponseEntity.ok(mapper.toDetail(memos));

    }
    //조회 (회의록 Id) <- 이거 없어도 무방, Minutes에서 같이 주면 됨
    @GetMapping("/{minutesId}/memos")
    public ResponseEntity<MemoResponse.Detail> findMemo(
            @PathVariable("minutesId") Long minutesId,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getId();
        List<Memo> memo = memoService.getMemo(minutesId, userId);
        return ResponseEntity.ok(mapper.toDetail(memo));
    }
    //수정 (회의록 Id, req, userId)
    @PatchMapping("/{minutesId}/memos")
    public ResponseEntity<MemoResponse.Detail> updateMemo(
            @PathVariable("minutesId") Long minutesId,
            @RequestBody MemoRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        List<Memo> memo = memoService.updateMemo(minutesId, request, user);
        return ResponseEntity.ok(mapper.toDetail(memo));
    }
    //삭제 (회의록 Id, userId)
    @DeleteMapping("/{minutesId}/memos")
    public ResponseEntity<MemoResponse.Detail> deleteMemo(
            @PathVariable("minutesId") Long minutesId,
            @ModelAttribute MemoRequest.Delete request,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        memoService.deleteMemo(minutesId, request, user);
        return ResponseEntity.noContent().build();
    }
}
