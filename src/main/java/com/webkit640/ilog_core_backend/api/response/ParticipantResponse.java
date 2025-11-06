package com.webkit640.ilog_core_backend.api.response;

import com.webkit640.ilog_core_backend.domain.model.MinutesParticipant;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;

public class ParticipantResponse {
    @Data
    @AllArgsConstructor
    public static class DetailLink<T>{
        Collection<T> participants;
        String link;
    }
    @Data
    @AllArgsConstructor
    public static class Detail<T>{
        Collection<T> participants;
    }

    @Data
    @AllArgsConstructor
    public static class MinutesParticipant{
        private Long id;
        private Long minutesId;
        private Long participantId;
        private String participantName;
    }

    @Data
    @AllArgsConstructor
    public static class FolderParticipant{
        private Long id;
        private Long folderId;
        private Long participantId;
        private String participantName;
    }

}
