package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.ParticipantLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantLogDAO extends JpaRepository<ParticipantLog, Long> {
}
