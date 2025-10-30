package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.MemoLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoLogDAO extends JpaRepository<MemoLog, Long> {
}
