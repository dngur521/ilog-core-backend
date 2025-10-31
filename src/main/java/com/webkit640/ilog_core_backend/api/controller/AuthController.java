package com.webkit640.ilog_core_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.api.request.AuthRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.application.service.AuthService;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //로그인
    @PostMapping("/login")
    public ResponseEntity<AuthResponse.Token> login(
            @RequestBody AuthRequest.Login request
    ) {
        AuthResponse.Token token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        authService.logout(request, user);
        return ResponseEntity.noContent().build();
    }

    //재인증 <- AI 돌리고 나온거라 굳이 필요한가 싶지만 그래도!!
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse.Token> refresh(
            @RequestBody AuthRequest.refresh body
    ) {
        String refresh = body.getRefreshToken();
        String newAccess = authService.refresh(body.getRefreshToken());
        return ResponseEntity.ok(new AuthResponse.Token(newAccess, refresh));
    }
}
