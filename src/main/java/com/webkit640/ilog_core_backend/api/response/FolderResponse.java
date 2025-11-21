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
        private String folderImage;
    }
    @Data
    @AllArgsConstructor
    public static class Find {
        private Long folderId;
        private String folderName;
        private List<FolderSummary> childFolders;
        private List<MinutesSummary> minutesList;
        private String folderImage;
    }
    @Data
    @AllArgsConstructor
    public static class FolderSummary {
        private Long id;
        private String name;
        private List<GetFolderParticipant> folderParticipants;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String folderImage;
    }

    @Data
    @AllArgsConstructor
    public static class GetFolderParticipant{
        private Long participantId;
        private String participantName;
        private String participantEmail;
        private String participantProfileImage;
        private LocalDateTime approachedAt;
    }
    @Data
    @AllArgsConstructor
    public static class MinutesSummary {
        private Long id;
        private String name;
        private List<GetMinutesParticipant> minutesParticipants;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @AllArgsConstructor
    public static class GetMinutesParticipant{
        private Long participantId;
        private String participantName;
        private String participantEmail;
        private String participantProfileImage;
        private LocalDateTime approachedAt;
    }

    @Data
    @AllArgsConstructor
    public static class FolderFlatDTO{
        Long folderId;
        String folderName;

        Long participantId;
        String participantName;
        String participantEmail;
        String participantProfileImage;
        LocalDateTime approachedAt;

        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        String folderImage;
    }


    @Data
    @AllArgsConstructor
    public static class MinutesFlatDTO{
        Long minutesId;
        String minutesName;

        Long participantId;
        String participantName;
        String participantEmail;
        String participantProfileImage;
        LocalDateTime approachedAt;

        LocalDateTime createdAt;
        LocalDateTime updatedAt;
        String folderImage;
    }
    @Data
    @AllArgsConstructor
    public static class Update {
        private Long folderId;
        private String folderName;
        private LocalDateTime updatedAt;
        private String folderImage;
    }
}
