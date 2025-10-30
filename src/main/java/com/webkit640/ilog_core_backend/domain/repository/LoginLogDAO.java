package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogDAO extends JpaRepository<LoginLog, Long> {
    List<LoginLog> findAllByUserId(Long userId);
}
