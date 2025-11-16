package com.webkit640.ilog_core_backend.domain.event;

import com.webkit640.ilog_core_backend.domain.model.ActionType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class MinutesLogEvent extends ApplicationEvent {

    private final Long userId;
    private final String email;
    private final LocalDateTime createdAt;
    private final String minutesTitle;
    private final String alterUserEmail;
    private final ActionType status;
    private final String description;

    public MinutesLogEvent(
            Object source, Long userId, String email,
            LocalDateTime createdAt, String minutesTitle,
            String alterUserEmail, ActionType status, String description
    ) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.createdAt = createdAt;
        this.minutesTitle = minutesTitle;
        this.alterUserEmail = alterUserEmail;
        this.status = status;
        this.description = description;
    }
}
