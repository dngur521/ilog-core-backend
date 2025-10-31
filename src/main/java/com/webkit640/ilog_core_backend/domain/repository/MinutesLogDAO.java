package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.MinutesLog;

public interface MinutesLogDAO extends JpaRepository<MinutesLog, Long> {

    List<MinutesLog> findAllByUserId(Long userId);
}
