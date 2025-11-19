package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.MemoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoHistoryDAO extends JpaRepository<MemoHistory, Long> {
    List<MemoHistory> findAllByMinutesHistory_Minutes_Id(Long minutesId);

    List<MemoHistory> findAllByMinutesHistory_Minutes_IdAndMinutesHistory_HistoryId(Long minutesId, Long historyId);
}
