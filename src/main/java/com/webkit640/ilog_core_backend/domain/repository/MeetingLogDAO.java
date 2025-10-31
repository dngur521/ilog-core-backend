package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.MeetingLog;

public interface MeetingLogDAO extends JpaRepository<MeetingLog, Long> {

    List<MeetingLog> findAllByUserId(Long userId);
}
