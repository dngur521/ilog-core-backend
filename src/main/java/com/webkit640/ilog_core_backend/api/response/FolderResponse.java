package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class FolderResponse {
    @Data
    @AllArgsConstructor
    public static class Create{
        private Long folderId;
        private String folderName;
        private LocalDateTime createdAt;
    }
    @Data
    @AllArgsConstructor
    public static class Find {
        private Long folderId;
        private String folderName;
        private List<FolderSummary> childFolders;
        private List<MinutesSummary> minutesList;
    }
    @Data
    @AllArgsConstructor
    public static class FolderSummary {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class MinutesSummary {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Update {
        private Long folderId;
        private String folderName;
        private LocalDateTime updatedAt;
    }
}
