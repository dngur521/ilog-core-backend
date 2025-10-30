package com.webkit640.ilog_core_backend.api.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(HttpStatus status, String message){
        this.status = status.value();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message){
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


}
