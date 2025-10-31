package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.LoginLog;

public interface LoginLogDAO extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findAllByUserId(Long userId);
}
