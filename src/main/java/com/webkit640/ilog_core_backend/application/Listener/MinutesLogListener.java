package com.webkit640.ilog_core_backend.application.Listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.webkit640.ilog_core_backend.domain.event.MinutesLogEvent;
import com.webkit640.ilog_core_backend.domain.model.MinutesLog;
import com.webkit640.ilog_core_backend.domain.repository.MinutesLogDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinutesLogListener {

    private final MinutesLogDAO minutesLogDAO;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMinutesLog(MinutesLogEvent event) {
        MinutesLog logEntity = new MinutesLog();

        logEntity.setUserId(event.getUserId());
        logEntity.setEmail(event.getEmail());
        logEntity.setCreatedAt(event.getCreatedAt());
        logEntity.setMinutesTitle(event.getMinutesTitle());
        logEntity.setAlterUserEmail(event.getAlterUserEmail());
        logEntity.setStatus(event.getStatus());
        logEntity.setDescription(event.getDescription());

        minutesLogDAO.save(logEntity);
    }
}
