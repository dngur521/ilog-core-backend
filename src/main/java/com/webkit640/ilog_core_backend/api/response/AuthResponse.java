package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;


public class AuthResponse {
    @Data
    @AllArgsConstructor
    public static class Token{
        private String accessToken;
        private String refreshToken;
    }
    @Data
    @AllArgsConstructor
    public static class ResetToken{
        private String resetToken;
    }
}
