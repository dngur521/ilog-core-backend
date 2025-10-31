package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MeetingRequest;
import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Meeting;
import com.webkit640.ilog_core_backend.domain.model.MeetingLog;
import com.webkit640.ilog_core_backend.domain.model.MeetingType;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.repository.MeetingDAO;
import com.webkit640.ilog_core_backend.domain.repository.MeetingLogDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MemberService memberService;
    private final MeetingDAO meetingDAO;
    private final MeetingLogDAO meetingLogDAO;
    private final MinutesService minutesService;

    //화상회의 생성
    @Transactional
    public Meeting createMeeting(Long ownerId) {
        Member owner = memberService.getMember(ownerId);

        //-------------------화상회의 저장-------------------
        Meeting meeting = new Meeting();
        meeting.setOwner(owner);
        meeting.setMeetingAddress("123"); // 내부 생성해야함 -> url
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setStatus(MeetingType.ONGOING);
        meetingDAO.save(meeting);

        //-------------------------로그------------------------------
        meetingLogging(owner.getId(), owner.getEmail(), meeting.getCreatedAt(), meeting.getId(), ActionType.CREATE, "정상 생성");

        return meeting;
    }

    //화상회의 참여
    @Transactional
    public Meeting joinMeeting(Long meetingId, MeetingRequest.Join request, CustomUserDetails participant) {
        Meeting meeting = getMeeting(meetingId);

        // -------------------회의의 주소가 맞는지 검증-------------------
        if (!meeting.getMeetingAddress().equals(request.getMeetingAddress())) {
            throw new CustomException(ErrorCode.ADDRESS_NOT_MATCH);
        }

        //--------------------------------로그 ---------------------------------------------
        meetingLogging(participant.getId(), participant.getUsername(), LocalDateTime.now(), meeting.getId(), ActionType.JOIN, "정상 참여");

        //--------------------지금 참여한 회의자의 회의록 폴더 권한이 없으면 추가해준다 ------------------------ 이거 회의록 위치 처음인지 나중인지 몰라서 나중에 처리함
        return meeting;
    }

    //화상회의 찾기
    private Meeting getMeeting(Long meetingId) {
        return meetingDAO.findById(meetingId).orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));
    }

    //화상회의 퇴장
    public void exitMeeting(Long meetingId, CustomUserDetails participant) {
        //--------------------------------로그 ---------------------------------------------
        meetingLogging(participant.getId(), participant.getUsername(), LocalDateTime.now(), meetingId, ActionType.DELETE, "정상 퇴장");
    }

    public Minutes endMeeting(Long meetingId, MeetingRequest.End request, Long ownerId) {
        Member owner = memberService.getMember(ownerId);
        Meeting meeting = getMeeting(meetingId);

        //request 형변환
        MinutesRequest.Create req = new MinutesRequest.Create();
        req.setTitle(req.getTitle());
        req.setContent(req.getContent());
        req.setStatus(req.getStatus());

        //회의록 생성
        Minutes minutes = minutesService.createMinutes(request.getFolderId(), req, ownerId);

        //------------------ 로그 -------------------
        meetingLogging(owner.getId(), owner.getEmail(), LocalDateTime.now(), meeting.getId(), ActionType.END, "화상회의 종료");
        //-------------- 회의 삭제 -------------------
        meetingDAO.delete(meeting);
        // 참여자 쫓아내야하는데 회의가 어떻게 흘러가는지도 잘 모르니 패스
        //생성된 회의 반환
        return minutes;
    }

    private void meetingLogging(Long userId, String email, LocalDateTime createdAt, Long meetingId, ActionType status, String description) {
        MeetingLog meetingLog = new MeetingLog();
        meetingLog.setUserId(userId);
        meetingLog.setEmail(email);
        meetingLog.setCreatedAt(createdAt);
        meetingLog.setMeetingId(meetingId);
        meetingLog.setStatus(status);
        meetingLog.setDescription(description);

        meetingLogDAO.save(meetingLog);
    }
}
