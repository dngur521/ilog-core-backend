package com.webkit640.ilog_core_backend.api.controller;

import com.webkit640.ilog_core_backend.application.mapper.AuthMapper;
import com.webkit640.ilog_core_backend.infrastructure.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import com.webkit640.ilog_core_backend.api.request.AuthRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.application.service.AuthService;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;
    //로그인
    @PostMapping("/login")
    public ResponseEntity<AuthResponse.Token> login(
            HttpServletRequest httpServletRequest,
            @RequestBody AuthRequest.Login request
    ){
        String clientIp = IpUtils.getClientIP(httpServletRequest);
        AuthResponse.Token token = authService.login(request, clientIp);
        return ResponseEntity.ok(authMapper.toToken(token.getAccessToken(),token.getRefreshToken()));
    }
    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ){
        authService.logout(request, user);
        return ResponseEntity.noContent().build();
    }
    //재인증
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse.Token> refresh(
            @RequestBody AuthRequest.refresh body
            ){
        AuthResponse.Token token = authService.refresh(body.getRefreshToken());
        return ResponseEntity.ok(authMapper.toToken(token.getAccessToken(),token.getRefreshToken()));
    }
}
