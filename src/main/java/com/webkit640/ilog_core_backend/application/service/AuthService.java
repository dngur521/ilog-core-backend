package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.AuthRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.LoginLog;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.LoginLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;
import com.webkit640.ilog_core_backend.infrastructure.security.JwtTokenProvider;
import com.webkit640.ilog_core_backend.infrastructure.security.TokenStoreService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    // private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDAO memberDAO;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogDAO loginLogDAO;
    private final TokenStoreService tokenStoreService;
    @Transactional
    public AuthResponse.Token login(AuthRequest.Login request) {
        //이메일로 회원인지 아닌지 파악
        Member member = memberDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //비밀번호 일치 파악
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String access = jwtTokenProvider.createAccessToken(member.getId(),member.getEmail(),List.of("USER"));
        String refresh = jwtTokenProvider.createRefreshToken(member.getId(),member.getEmail());

        Long refreshTtlSec = (jwtTokenProvider.getExpiration(refresh).getTime()- System.currentTimeMillis())/ 1000;
        tokenStoreService.saveRefresh(member.getId(), refresh, refreshTtlSec);

        //--------------------- 로그 --------------------------
        loginLogging(member.getId(), member.getEmail(),ActionType.LOGIN,"정상 로그인");
        //------------------- 응답 반환 ------------------------
        return new AuthResponse.Token(access, refresh);
    }
    @Transactional
    public void logout(HttpServletRequest request, CustomUserDetails user) {
        String header = request.getHeader("Authorization");
        if(header == null || header.isBlank() || !header.startsWith("Bearer ")){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }else{
            String access = header.substring(7);
            String jti = jwtTokenProvider.getJti(access);
            long ttlSec = Math.max(1,(jwtTokenProvider.getExpiration(access).getTime() - System.currentTimeMillis())/ 1000);
            tokenStoreService.blacklistAccess(jti, ttlSec);
        }
        if(user.getId() == null){
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }else{
            tokenStoreService.deleteRefresh(user.getId());
        }

        //--------------------- 로그 --------------------------
        loginLogging(user.getId(),user.getUsername(),ActionType.LOGOUT,"정상 로그아웃");
    }

    public String refresh(String refresh) {
        if(!jwtTokenProvider.isTokenValid(refresh) || !"refresh".equals(jwtTokenProvider.getType(refresh))){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtTokenProvider.getUserId(refresh);
        String stored = tokenStoreService.getRefresh(userId);
        if(stored == null || !stored.equals(refresh)){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        String email = jwtTokenProvider.getUsername(refresh);
        return jwtTokenProvider.createAccessToken(userId,email, List.of("USER"));
    }

    private void loginLogging(Long userId, String email, ActionType actionType, String description){
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setEmail(email);
        loginLog.setCreatedAt(LocalDateTime.now());
        loginLog.setStatus(actionType);
        loginLog.setDescription(description);

        loginLogDAO.save(loginLog);
    }

}
