package com.webkit640.ilog_core_backend.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ParticipantRequest {

    @Data
    public static class Create {
        @NotBlank
        private String createMemberEmail;
    }

    @Data
    public static class Delete {
        private Long deleteMemberId;
    }
}
