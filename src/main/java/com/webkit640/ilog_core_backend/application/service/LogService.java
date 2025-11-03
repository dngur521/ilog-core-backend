package com.webkit640.ilog_core_backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.webkit640.ilog_core_backend.domain.model.LoginLog;
import com.webkit640.ilog_core_backend.domain.model.MeetingLog;
import com.webkit640.ilog_core_backend.domain.model.MinutesLog;
import com.webkit640.ilog_core_backend.domain.repository.LoginLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.MeetingLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.MinutesLogDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LoginLogDAO loginLogDAO;
    // private final FolderLogDAO folderLogDAO;
    private final MeetingLogDAO meetingLogDAO;
    private final MinutesLogDAO minutesLogDAO;
    // private final MemoLogDAO memoLogDAO;
    // private final ParticipantLogDAO participantLogDAO;
    public List<LoginLog> getLoginLog(Long userId) {
        return loginLogDAO.findAllByUserId(userId);
    }

    public List<MeetingLog> getMeetingLog(Long userId) {
        return meetingLogDAO.findAllByUserId(userId);
    }

    public List<MinutesLog> getMinutesLog(Long userId) {
        return minutesLogDAO.findAllByUserId(userId);
    }
}
