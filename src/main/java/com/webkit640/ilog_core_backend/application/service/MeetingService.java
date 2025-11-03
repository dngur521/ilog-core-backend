package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.request.MeetingRequest;
import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.MeetingLog;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.repository.MeetingLogDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MemberService memberService;
    private final MeetingLogDAO meetingLogDAO;
    private final MinutesService minutesService;

    //화상회의 생성
    @Transactional
    public void createMeeting(CustomUserDetails owner) {
        //-------------------------로그------------------------------
        meetingLogging(owner.getId(), owner.getUsername(), LocalDateTime.now(), ActionType.CREATE, "정상 생성");
    }

    //화상회의 참여
    @Transactional
    public void joinMeeting(CustomUserDetails participant) {
        //--------------------------------로그 ---------------------------------------------
        meetingLogging(participant.getId(), participant.getUsername(), LocalDateTime.now(), ActionType.JOIN, "정상 참여");
    }

    //화상회의 퇴장
    public void exitMeeting(CustomUserDetails participant) {
        //--------------------------------로그 ---------------------------------------------
        meetingLogging(participant.getId(), participant.getUsername(), LocalDateTime.now(), ActionType.DELETE, "정상 퇴장");
    }

    //화상회의 종료
    public Minutes endMeeting(MeetingRequest.End request, Long ownerId) {
        Member owner = memberService.getMember(ownerId);

        //request 형변환
        MinutesRequest.Create req = new MinutesRequest.Create();
        req.setTitle(req.getTitle());
        req.setContent(req.getContent());
        req.setStatus(req.getStatus());

        //회의록 생성
        Minutes minutes = minutesService.createMinutes(request.getFolderId(), req, ownerId);

        //------------------ 로그 -------------------
        meetingLogging(owner.getId(), owner.getEmail(), LocalDateTime.now(), ActionType.END, "화상회의 종료");
        //생성된 회의 반환
        return minutes;
    }

    private void meetingLogging(Long userId, String email, LocalDateTime createdAt, ActionType status, String description) {
        MeetingLog meetingLog = new MeetingLog();
        meetingLog.setUserId(userId);
        meetingLog.setEmail(email);
        meetingLog.setCreatedAt(createdAt);
        meetingLog.setStatus(status);
        meetingLog.setDescription(description);

        meetingLogDAO.save(meetingLog);
    }
}
