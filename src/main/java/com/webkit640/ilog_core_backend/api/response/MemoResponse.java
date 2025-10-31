package com.webkit640.ilog_core_backend.api.response;

import java.time.LocalDateTime;
import java.util.List;

import com.webkit640.ilog_core_backend.domain.model.MemoType;

import lombok.AllArgsConstructor;
import lombok.Data;

public class MemoResponse {

    @Data
    @AllArgsConstructor
    public static class Detail {

        private List<Summary> memos;
    }

    @Data
    @AllArgsConstructor
    public static class Summary {

        private Long id;
        private String name;
        private String content;
        private MemoType memoType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
