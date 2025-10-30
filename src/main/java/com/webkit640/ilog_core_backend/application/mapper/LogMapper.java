package com.webkit640.ilog_core_backend.application.mapper;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import com.webkit640.ilog_core_backend.domain.model.CommonLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogMapper{
    public <T extends CommonLog> LogResponse.Detail toResponse(List<T> logs){
        List<LogResponse.Log> mappedLogs = logs.stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new LogResponse.Detail(mappedLogs);
    }

    private <T extends CommonLog> LogResponse.Log convert(T log){
        return new LogResponse.Log(
                log.getId(),
                log.getCreatedAt(),
                log.getStatus()
        );
    }
}
