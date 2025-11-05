package com.webkit640.ilog_core_backend.application.service;

import java.util.List;

import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.domain.repository.*;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LoginLogDAO loginLogDAO;
     private final FolderLogDAO folderLogDAO;
    private final MeetingLogDAO meetingLogDAO;
    private final MinutesLogDAO minutesLogDAO;
     private final MemoLogDAO memoLogDAO;
     private final ParticipantLogDAO participantLogDAO;
    public List<LoginLog> getLoginLog(Long userId) {
        return loginLogDAO.findAllByUserId(userId);
    }

    public List<MeetingLog> getMeetingLog(Long userId) {
        return meetingLogDAO.findAllByUserId(userId);
    }

    public List<MinutesLog> getMinutesLog(Long userId) {
        return minutesLogDAO.findAllByUserId(userId);
    }

    public List<FolderLog> getFolderLog(Long userId) {
        return folderLogDAO.findAllByUserId(userId);
    }

    public List<MemoLog> getMemoLog(Long userId) {
        return memoLogDAO.findAllByUserId(userId);
    }

    public List<ParticipantLog> getParticipantLog(Long userId) {
        return participantLogDAO.findAllByUserId(userId);
    }
}
