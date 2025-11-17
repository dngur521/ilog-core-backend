package com.webkit640.ilog_core_backend.domain.model;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MeetingLog extends CommonLog {
    @Override
    public LogResponse.Log toDto(){
        LogResponse.Log dto = super.toDto();
        dto.setMinutesTitle(null);
        dto.setIpAddress(null);
        return dto;
    }
}
