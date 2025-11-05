package com.webkit640.ilog_core_backend.api.controller;

import java.util.List;

import com.webkit640.ilog_core_backend.domain.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import com.webkit640.ilog_core_backend.application.mapper.LogMapper;
import com.webkit640.ilog_core_backend.application.service.LogService;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/logs")
public class LogController {

    private LogService logService;
    private LogMapper logMapper;

    // 내 로그인기록 조회
    @GetMapping("/login")
    public ResponseEntity<LogResponse.Detail> findLoginLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<LoginLog> loginLogs = logService.getLoginLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(loginLogs));
    }

    // 내 화상채팅 조회
    @GetMapping("/meeting")
    public ResponseEntity<LogResponse.Detail> findMeetingLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<MeetingLog> meetingLogs = logService.getMeetingLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(meetingLogs));
    }

    // 내 회의록 조회
    @GetMapping("/minutes")
    public ResponseEntity<LogResponse.Detail> findMinutesLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<MinutesLog> minutesLogs = logService.getMinutesLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(minutesLogs));
    }

    // 내 폴더 조회
    @GetMapping("/folders")
    public ResponseEntity<LogResponse.Detail> findFoldersLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<FolderLog> folderLogs = logService.getFolderLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(folderLogs));
    }

    // 내 메모 조회
    @GetMapping("/memos")
    public ResponseEntity<LogResponse.Detail> findMemoLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<MemoLog> memoLogs = logService.getMemoLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(memoLogs));
    }

    // 내 참가자 조회
    @GetMapping("/participant")
    public ResponseEntity<LogResponse.Detail> findParticipantLog(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getId();
        List<ParticipantLog> participantLogs = logService.getParticipantLog(userId);
        return ResponseEntity.ok(logMapper.toResponse(participantLogs));
    }
}
