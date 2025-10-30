package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;


public class AuthResponse {
    @Data
    @AllArgsConstructor
    public static class Token{
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
         public Token(String accessToken, String refreshToken){
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = "Bearer";
        }
    }


}
