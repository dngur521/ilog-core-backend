package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class MemberResponse {
    @Data
    @AllArgsConstructor
    public static class Detail{
        private Long memberId;
        private String email;
        private String name;
        private String phoneNum;
        private LocalDateTime joinedAt;
        private List<FolderSummary> folders;
    }

    @Data
    @AllArgsConstructor
    public static class FolderSummary{
        private Long id;
        private String name;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    public static class Email{
        private String email;
    }
}
