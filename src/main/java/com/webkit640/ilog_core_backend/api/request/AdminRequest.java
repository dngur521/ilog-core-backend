package com.webkit640.ilog_core_backend.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminRequest {
    @Data
    public static class Update {
        @NotBlank
        private Long id;
        private String name;
        private String newPassword;
        private String checkPassword;
    }
}
