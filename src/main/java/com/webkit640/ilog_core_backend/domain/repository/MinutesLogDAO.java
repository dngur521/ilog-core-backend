package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.MinutesLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinutesLogDAO extends JpaRepository<MinutesLog,Long> {
    List<MinutesLog> findAllByUserId(Long userId);
}
