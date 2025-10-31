package com.webkit640.ilog_core_backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import com.webkit640.ilog_core_backend.domain.model.MinutesParticipant;

public interface MinutesParticipantDAO extends JpaRepository<MinutesParticipant, Long> {

    Optional<MinutesParticipant> findByMinutesAndParticipant(Minutes minutes, Member participant);

    List<MinutesParticipant> findByMinutes(Minutes minutes);

    boolean existsByMinutesAndParticipant(Minutes minutes, Member user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from MinutesParticipant mp where mp.minutes = :minutes and mp.participant = :participant")
    void deleteByMinutesAndParticipant(@Param("minutes") Minutes minutes,
            @Param("participant") Member participant);
}
