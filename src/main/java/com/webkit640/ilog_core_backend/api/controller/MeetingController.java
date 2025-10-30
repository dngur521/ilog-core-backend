package com.webkit640.ilog_core_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.MeetingRequest;
import com.webkit640.ilog_core_backend.api.response.MeetingResponse;
import com.webkit640.ilog_core_backend.application.mapper.MeetingMapper;
import com.webkit640.ilog_core_backend.application.service.MeetingService;
import com.webkit640.ilog_core_backend.domain.model.Meeting;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetings")
public class MeetingController {
    private final MeetingService meetingService;
    private final MeetingMapper mapper;
    //화상회의 생성
    @PostMapping
    public ResponseEntity<MeetingResponse.Create> createMeeting(
            @AuthenticationPrincipal CustomUserDetails owner
            ){
        Long ownerId = owner.getId();
        Meeting meeting = meetingService.createMeeting(ownerId);
        //생성 주소
        return ResponseEntity.ok(mapper.toCreate(meeting));
    }
    //화상회의 참여
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingResponse.Join> joinMeeting(
            @PathVariable("meetingId") Long meetingId,
            @RequestBody MeetingRequest.Join request,
            @AuthenticationPrincipal CustomUserDetails participant
    ){
        Meeting meeting = meetingService.joinMeeting(meetingId,request,participant);
        return ResponseEntity.ok(mapper.toJoin(meeting));
    }

    //화상회의 퇴장 (참여자)
    @DeleteMapping("/{meetingId}/exit")
    public ResponseEntity<Void> exitMeeting(
        @PathVariable("meetingId") Long meetingId,
        @AuthenticationPrincipal CustomUserDetails participant
    ){
        meetingService.exitMeeting(meetingId, participant);
        return ResponseEntity.noContent().build();
    }

    //회상회의 종료 (주최자) <- 이건 말이 안됨, 퇴장으로 하나로 합치고 거기서 권한에 따라 다른 서비스 제공으로 바꿔야함
    @PostMapping("/{meetingId}/end")
    public ResponseEntity<MeetingResponse.End> endMeeting(
        @PathVariable("meetingId") Long meetingId,
        @RequestBody MeetingRequest.End request,
        @AuthenticationPrincipal CustomUserDetails owner
    ){
        Long ownerId = owner.getId();
        Minutes minutes = meetingService.endMeeting(meetingId, request, ownerId);
        return ResponseEntity.ok(mapper.toEnd(minutes));
    }
}
