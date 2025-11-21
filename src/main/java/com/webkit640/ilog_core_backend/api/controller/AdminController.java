package com.webkit640.ilog_core_backend.api.controller;


import com.webkit640.ilog_core_backend.api.request.AdminRequest;
import com.webkit640.ilog_core_backend.api.response.MemberResponse;
import com.webkit640.ilog_core_backend.application.mapper.MemberMapper;
import com.webkit640.ilog_core_backend.application.service.MemberService;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    //모든 회원 조회
    @GetMapping
    public ResponseEntity<List<Member>> findMember(
            @AuthenticationPrincipal CustomUserDetails admin
    ){
        Long adminId = admin.getId();
        List<Member> member = memberService.getAllMember(adminId);
        return ResponseEntity.ok(member);
    }

    //특정 회원 수정
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberResponse.Detail> updateMember(
            @ModelAttribute AdminRequest.Update request,
            @RequestPart(value = "profileImage",required=false) MultipartFile profileImage,
            @AuthenticationPrincipal CustomUserDetails admin
    ){
        Long adminId = admin.getId();
        Member member = memberService.alterMember(request,adminId, profileImage);
        return ResponseEntity.ok(memberMapper.toFind(member));
    }

    //특정 회원 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails admin
    ){
        Long adminId = admin.getId();
        memberService.deleteMember(memberId, adminId);
        return ResponseEntity.noContent().build();
    }
}
