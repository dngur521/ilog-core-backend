package com.webkit640.ilog_core_backend.api.request;

import lombok.Data;

public class ParticipantRequest {

    @Data
    public static class Create {

        private Long createMemberId;
    }

    @Data
    public static class Delete {

        private Long deleteMemberId;
    }
}
