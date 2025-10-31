package com.webkit640.ilog_core_backend.api.response;

import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.MemoType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
        private List<Memos> memos;
    }

    @Data
    @AllArgsConstructor
    public static class FindSummary {
        private Long id;
        private String title;
        private String summary;
        private List<Memos> memos;
    }

    @Data
    @AllArgsConstructor
    public static class Memos{
        private Long id;
        private String email;
        private String name;
        private MemoType memoType;
        private String content;
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
