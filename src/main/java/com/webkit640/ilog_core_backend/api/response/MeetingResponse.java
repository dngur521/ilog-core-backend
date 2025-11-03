package com.webkit640.ilog_core_backend.api.response;


import lombok.AllArgsConstructor;
import lombok.Data;

public class MeetingResponse {
    @Data
    @AllArgsConstructor
    public static class End {
        private Long id;
        private String title;
        private String content;
        private String summary;
    }
}
