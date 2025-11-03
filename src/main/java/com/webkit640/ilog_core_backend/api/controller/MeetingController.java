package com.webkit640.ilog_core_backend.api.controller;

import com.webkit640.ilog_core_backend.application.mapper.MeetingMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.MeetingRequest;
import com.webkit640.ilog_core_backend.api.response.MeetingResponse;
import com.webkit640.ilog_core_backend.application.service.MeetingService;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings")
public class MeetingController {
    private final MeetingMapper mapper;
    private final MeetingService meetingService;

    //화상회의 생성
    @PostMapping
    public ResponseEntity<Void> createMeeting(
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        meetingService.createMeeting(owner);
        return ResponseEntity.noContent().build();
    }

    //화상회의 참여
    @PostMapping("/join")
    public ResponseEntity<Void> joinMeeting(
            @AuthenticationPrincipal CustomUserDetails participant
    ) {
        meetingService.joinMeeting(participant);
        return ResponseEntity.noContent().build();
    }

    //화상회의 퇴장 (참여자)
    @DeleteMapping("/exit")
    public ResponseEntity<Void> exitMeeting(
            @AuthenticationPrincipal CustomUserDetails participant
    ) {
        meetingService.exitMeeting(participant);
        return ResponseEntity.noContent().build();
    }

    //회상회의 종료 (주최자)
    @PostMapping("/end")
    public ResponseEntity<MeetingResponse.End> endMeeting(
            @RequestBody MeetingRequest.End request,
            @AuthenticationPrincipal CustomUserDetails owner
    ) {
        Long ownerId = owner.getId();
        Minutes minutes = meetingService.endMeeting(request, ownerId);
        return ResponseEntity.ok(mapper.toEnd(minutes));
    }
}
