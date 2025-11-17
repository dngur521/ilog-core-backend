package com.webkit640.ilog_core_backend.domain.model;

import com.webkit640.ilog_core_backend.api.response.LogResponse;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LoginLog extends CommonLog {
    private String ipAddress;

    @Override
    public LogResponse.Log toDto(){
        LogResponse.Log dto = super.toDto();
        dto.setMinutesTitle(null);
        dto.setIpAddress(ipAddress);
        return dto;
    }
}
