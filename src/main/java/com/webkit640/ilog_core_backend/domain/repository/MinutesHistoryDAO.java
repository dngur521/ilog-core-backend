package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.MinutesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MinutesHistoryDAO extends JpaRepository<MinutesHistory, Long> {

    List<MinutesHistory> findAllByMinutes_Id(Long minutesId);

    Optional<MinutesHistory> findByMinutes_IdAndHistoryId(Long minutesId, Long historyId);

    @Query("SELECT MAX(h.historyId) FROM MinutesHistory h WHERE h.minutes.id = :minutesId")
    Optional<Long> findMaxHistoryIdByMinutesId(@Param("minutesId") Long minutesId);
}
