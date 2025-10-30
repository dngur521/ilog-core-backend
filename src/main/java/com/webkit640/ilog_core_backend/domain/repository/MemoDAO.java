package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoDAO extends JpaRepository<Memo, Long> {
    List<Memo> findAllByMinutes(Minutes minutes);
}
