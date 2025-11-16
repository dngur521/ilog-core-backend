package com.webkit640.ilog_core_backend.api.response;

import com.webkit640.ilog_core_backend.domain.model.Memo;
import com.webkit640.ilog_core_backend.domain.model.MemoType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.cglib.core.Local;

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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
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
        private Integer startIndex;
        private Integer endIndex;
        private String positionContent;
    }

    @Data
    @AllArgsConstructor
    public static class Update {
        private Long id;
        private String title;
        private String content;
        private String summary;
    }

    @Data
    @AllArgsConstructor
    public static class FindHistory {
        private Long id;
        private Long minutesId;
        private Long historyId;
        private String title;
        private String content;
        private String summary;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Memos> memos;
    }

    @Data
    @AllArgsConstructor
    public static class Lock {
        private String token;
    }

    @Data
    @AllArgsConstructor
    public static class LockStatus{
        private boolean locked;
        private Long remainSeconds;
        private Long userId;
        private String userName;
    }
}
