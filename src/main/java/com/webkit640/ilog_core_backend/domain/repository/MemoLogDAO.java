package com.webkit640.ilog_core_backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.MemoLog;

import java.util.List;

public interface MemoLogDAO extends JpaRepository<MemoLog, Long> {
    List<MemoLog> findAllByUserId(Long userId);
}
