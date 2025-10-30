package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

public class MinutesResponse {
    @Data
    @AllArgsConstructor
    public static class Create{
        private Long id;
        private String title;
        private String content;
        private String summary;
    }
    @Data
    @AllArgsConstructor
    public static class FindContent {
        private Long id;
        private String title;
        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class FindSummary {
        private Long id;
        private String title;
        private String summary;
    }

    @Data
    @AllArgsConstructor
    public static class Update {
        private Long id;
        private String title;
        private String content;
        private String summary;
    }
}
