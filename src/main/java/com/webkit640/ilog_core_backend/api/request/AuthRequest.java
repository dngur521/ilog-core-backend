package com.webkit640.ilog_core_backend.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthRequest {

    @Data
    public static class Login {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class refresh {
        @NotBlank
        private String refreshToken;
    }
}
