package com.webkit640.ilog_core_backend.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.webkit640.ilog_core_backend.api.request.MemberRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.api.response.MemberResponse;
import com.webkit640.ilog_core_backend.application.mapper.AuthMapper;
import com.webkit640.ilog_core_backend.application.mapper.MemberMapper;
import com.webkit640.ilog_core_backend.application.service.MemberService;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final AuthMapper authMapper;

    //회원 가입
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberResponse.Create> registerMember(
            @ModelAttribute MemberRequest.Create request,
            @RequestPart(value = "profileImage",required=false) MultipartFile profileImage
    ){
        Member member = memberService.registerMember(request, profileImage);
        return ResponseEntity.ok(memberMapper.toCreate(member));
    }

    //회원 조회
    @GetMapping
    public ResponseEntity<MemberResponse.Detail> findMember(
            @AuthenticationPrincipal CustomUserDetails currentMember
    ){
        Long currentMemberId = currentMember.getId();
        Member member = memberService.getMember(currentMemberId);
        return ResponseEntity.ok(memberMapper.toFind(member));
    }

    //회원 수정
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberResponse.Detail> updateMember(
            @ModelAttribute MemberRequest.Update request,
            @RequestPart(value = "profileImage",required=false) MultipartFile profileImage,
            @AuthenticationPrincipal CustomUserDetails currentMember
    ){
        Long currentMemberId = currentMember.getId();
        Member member = memberService.updateMember(request,currentMemberId, profileImage);
        return ResponseEntity.ok(memberMapper.toFind(member));
    }


    //회원 비밀번호 입력 (아.. /verify로 할껄, 회원 수정이라서 password까지 갈 필요 없을 것 같은데)
    @PostMapping("/password/input")
    public ResponseEntity<Void> inputPassword(
            @RequestBody MemberRequest.inputPassword request,
            @AuthenticationPrincipal CustomUserDetails currentMember
    ){
        Long currentMemberId = currentMember.getId();
        memberService.inputPassword(request,currentMemberId);
        return ResponseEntity.noContent().build();
    }


    //회원 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentMember
    ){
        Long currentMemberId = currentMember.getId();
        memberService.deleteMember(memberId, currentMemberId);
        return ResponseEntity.noContent().build();
    }

    //이메일 찾기
    @PostMapping("/find-email")
    public ResponseEntity<List<MemberResponse.Email>> findEmail(
            @RequestBody MemberRequest.PhoneNum phoneNum
    ){
        List<String> email = memberService.getEmail(phoneNum.getPhoneNum());
        return ResponseEntity.ok(memberMapper.toEmail(email));
    }

    //비밀번호 재설정용 계정 검증(이메일+전화번호)
    @PostMapping("/password/verify")
    public ResponseEntity<AuthResponse.ResetToken> verifyAccount(
            @RequestBody MemberRequest.Verify request
    ){
        String resetToken = memberService.verifyAccount(request);
        return ResponseEntity.ok(authMapper.toResetToken(resetToken));
    }

    //비밀번호 찾기(새 비밀번호 저장)
    @PatchMapping("/password/reset")
    public ResponseEntity<MemberResponse.Detail> resetPassword(
        @RequestBody MemberRequest.Reset request
    ){
        Member member = memberService.resetPassword(request);
        return ResponseEntity.ok(memberMapper.toFind(member));
    }
}
