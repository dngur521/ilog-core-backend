package com.webkit640.ilog_core_backend.domain.model;

import java.time.LocalDateTime;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class CommonLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String email;
    private LocalDateTime createdAt;
    private String description;
    private ActionType status;

    public LogResponse.Log toDto(){
        return new LogResponse.Log(id,createdAt, status, null, null);
    }
}
