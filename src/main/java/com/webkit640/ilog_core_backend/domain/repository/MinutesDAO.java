package com.webkit640.ilog_core_backend.domain.repository;

import com.webkit640.ilog_core_backend.domain.model.Folder;
import com.webkit640.ilog_core_backend.domain.model.Minutes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinutesDAO extends JpaRepository<Minutes, Long> {
    List<Minutes> findByFolder(Folder folder);
}
