package com.webkit640.ilog_core_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.MemberRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.api.response.MemberResponse;
import com.webkit640.ilog_core_backend.application.mapper.AuthMapper;
import com.webkit640.ilog_core_backend.application.mapper.MemberMapper;
import com.webkit640.ilog_core_backend.application.service.MemberService;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final AuthMapper authMapper;

    //회원 가입
    @PostMapping
    public ResponseEntity<MemberResponse.Detail> registerMember(@RequestBody MemberRequest.Create request) {
        Member member = memberService.registerMember(request);
        return ResponseEntity.ok(memberMapper.toDetail(member));
    }

    //회원 조회 // 본인 확인 미완
    @GetMapping
    public ResponseEntity<MemberResponse.Detail> findMember(
            @AuthenticationPrincipal CustomUserDetails currentMember
    ) {
        Long currentMemberId = currentMember.getId();
        Member member = memberService.getMember(currentMemberId);
        return ResponseEntity.ok(memberMapper.toDetail(member));
    }

    //회원 수정
    @PatchMapping
    public ResponseEntity<MemberResponse.Detail> updateMember(
            @RequestBody MemberRequest.Update request,
            @AuthenticationPrincipal CustomUserDetails currentMember
    ) {
        Long currentMemberId = currentMember.getId();
        Member member = memberService.updateMember(request, currentMemberId);
        return ResponseEntity.ok(memberMapper.toDetail(member));
    }

    //회원 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentMember
    ) {
        Long currentMemberId = currentMember.getId();
        memberService.deleteMember(memberId, currentMemberId);
        return ResponseEntity.noContent().build();
    }

    //이메일 찾기
    @PostMapping("/find-email")
    public ResponseEntity<MemberResponse.Email> findEmail(
            @RequestBody MemberRequest.PhoneNum phoneNum
    ) {
        String email = memberService.getEmail(phoneNum.getPhoneNum());
        return ResponseEntity.ok(memberMapper.toEmail(email));
    }

    //비밀번호 재설정용 계정 검증(이메일+전화번호)
    @PostMapping("/password/verify")
    public ResponseEntity<AuthResponse.Token> verifyAccount(
            @RequestBody MemberRequest.Verify request
    ) {
        String resetToken = memberService.verifyAccount(request);
        return ResponseEntity.ok(authMapper.toToken(resetToken, resetToken));
    }

    //비밀번호 찾기(새 비밀번호 저장)
    @PatchMapping("/password/reset")
    public ResponseEntity<MemberResponse.Detail> resetPassword(
            @RequestBody MemberRequest.Reset request
    ) {
        Member member = memberService.resetPassword(request);
        return ResponseEntity.ok(memberMapper.toDetail(member));
    }
}
