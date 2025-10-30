package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDAO extends JpaRepository<Meeting, Long> {
}
