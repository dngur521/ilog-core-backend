package com.webkit640.ilog_core_backend.application.mapper;

import org.springframework.stereotype.Component;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;

@Component
public class AuthMapper {

    public AuthResponse.Token toToken(String accessToken, String refreshToken) {
        if (accessToken == null || accessToken.isEmpty()
                || refreshToken == null || refreshToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return new AuthResponse.Token(accessToken, refreshToken);
    }
}
