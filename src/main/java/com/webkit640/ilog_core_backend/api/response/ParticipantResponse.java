package com.webkit640.ilog_core_backend.api.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ParticipantResponse {

    @Data
    @AllArgsConstructor
    public static class Detail {

        List<Participant> participants;
    }

    @Data
    @AllArgsConstructor
    public static class Participant {

        private Long id;
        private Long folderOrMinutesId; //에반데
        private Long participantId;
    }

}
