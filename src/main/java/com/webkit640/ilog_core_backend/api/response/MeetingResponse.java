package com.webkit640.ilog_core_backend.api.response;

import com.webkit640.ilog_core_backend.domain.model.MeetingType;

import lombok.AllArgsConstructor;
import lombok.Data;

public class MeetingResponse {

    @Data
    @AllArgsConstructor
    public static class Create {

        private String meetingAddress;
        private MeetingType status;
    }

    @Data
    @AllArgsConstructor
    public static class Join {

        private Long id;
    }

    @Data
    @AllArgsConstructor
    public static class End {

        private Long id;
    }
}
