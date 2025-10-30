package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.MeetingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingLogDAO extends JpaRepository<MeetingLog, Long> {
    List<MeetingLog> findAllByUserId(Long userId);
}
