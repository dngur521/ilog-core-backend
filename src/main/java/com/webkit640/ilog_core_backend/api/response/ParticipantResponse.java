package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class ParticipantResponse {
    @Data
    @AllArgsConstructor
    public static class Detail{
        List<Participant> participants;
    }

    @Data
    @AllArgsConstructor
    public static class Participant{
        private Long id;
        private Long folderOrMinutesId; //에반데
        private Long participantId;
    }


}
