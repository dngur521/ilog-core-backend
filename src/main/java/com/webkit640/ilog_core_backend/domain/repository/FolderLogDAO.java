package com.webkit640.ilog_core_backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webkit640.ilog_core_backend.domain.model.FolderLog;

import java.util.List;

public interface FolderLogDAO extends JpaRepository<FolderLog, Long> {
    List<FolderLog> findAllByUserId(Long userId);
}
