package com.webkit640.ilog_core_backend.application.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import com.webkit640.ilog_core_backend.domain.model.CommonLog;

@Component
public class LogMapper {

    public <T extends CommonLog> LogResponse.Detail toResponse(List<T> logs) {
        return new LogResponse.Detail(
                logs.stream().map(CommonLog::toDto).toList());
    }
}
