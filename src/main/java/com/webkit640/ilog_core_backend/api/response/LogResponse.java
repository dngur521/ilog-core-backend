package com.webkit640.ilog_core_backend.api.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webkit640.ilog_core_backend.domain.model.ActionType;

import lombok.AllArgsConstructor;
import lombok.Data;

public class LogResponse {

    @Data
    @AllArgsConstructor
    public static class Detail {
        private List<Log> logs;
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(Include.NON_NULL)
    public static class Log {
        private Long id;
        private LocalDateTime createdAt;
        private ActionType status;
        private String minutesTitle;
        private String ipAddress;
    }
}
