package com.webkit640.ilog_core_backend.api.request;

import com.webkit640.ilog_core_backend.domain.model.MinutesType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class MeetingRequest {
    @Data
    public static class End {
        @NotBlank
        private Long folderId;
        private String title;
        private String content;
        @NotBlank
        private MinutesType status;
    }
}
