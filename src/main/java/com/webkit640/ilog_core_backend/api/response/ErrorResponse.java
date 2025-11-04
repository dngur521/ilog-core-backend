package com.webkit640.ilog_core_backend.api.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.code = status.name();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
        this.timestamp = LocalDateTime.now();
    }

}
