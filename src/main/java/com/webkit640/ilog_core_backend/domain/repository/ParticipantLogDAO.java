package com.webkit640.ilog_core_backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.ParticipantLog;

import java.util.List;

public interface ParticipantLogDAO extends JpaRepository<ParticipantLog, Long> {
    List<ParticipantLog> findAllByUserId(Long userId);
}
