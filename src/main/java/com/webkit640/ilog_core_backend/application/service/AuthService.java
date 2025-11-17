package com.webkit640.ilog_core_backend.application.service;

import java.time.LocalDateTime;
import java.util.List;

import com.webkit640.ilog_core_backend.domain.model.*;
import com.webkit640.ilog_core_backend.infrastructure.util.IpUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.AuthRequest;
import com.webkit640.ilog_core_backend.api.response.AuthResponse;
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
    public AuthResponse.Token login(AuthRequest.Login request, String clientIp) {
        //이메일로 회원인지 아닌지 파악
        Member member = memberDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        //비밀번호 일치 파악
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        //-------------------- role이 추가되기 전에 만든 계정때문에 있는 코드, 나중에 없애면 됨 ---------------------------
        RoleType roleType = member.getRole() != null ? member.getRole() : RoleType.USER;
        //---------------------------------------------------------------------------------------------------------
        String access = jwtTokenProvider.createAccessToken(member.getId(),List.of("ROLE_" + roleType.name()));
        String refresh = jwtTokenProvider.createRefreshToken(member.getId());

        AuthResponse.Token token = new AuthResponse.Token(access,refresh);

        Long refreshTtlSec = (jwtTokenProvider.getExpiration(refresh).getTime()- System.currentTimeMillis())/ 1000;
        tokenStoreService.saveRefresh(member.getId(), refresh, refreshTtlSec);

        //--------------------- 로그 --------------------------
        loginLogging(member.getId(), member.getEmail(),ActionType.LOGIN,"정상 로그인", clientIp);
        //------------------- 응답 반환 ------------------------
        return token;
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
        Member member = memberDAO.findById(user.getId()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        //--------------------- 로그 --------------------------
        String clientIp = IpUtils.getClientIP(request);
        loginLogging(member.getId(),member.getEmail(),ActionType.LOGOUT,"정상 로그아웃", clientIp);
    }

    public AuthResponse.Token refresh(String refresh) {
        if(!jwtTokenProvider.isTokenValid(refresh) || !"REFRESH".equals(jwtTokenProvider.getType(refresh))){
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(refresh);
        String stored = tokenStoreService.getRefresh(userId);

        if(!refresh.equals(stored))
            throw new CustomException(ErrorCode.INVALID_TOKEN);

        Member member = memberDAO.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        //-------------------- role이 추가되기 전에 만든 계정때문에 있는 코드, 나중에 없애면 됨 ---------------------------
        RoleType roleType = member.getRole() != null ? member.getRole() : RoleType.USER;

        String newAccess = jwtTokenProvider.createAccessToken(userId,List.of("ROLE_" + roleType.name()));
//        String newRefresh = jwtTokenProvider.createRefreshToken(userId);

        Long ttlSec = (jwtTokenProvider.getExpiration(refresh).getTime() - System.currentTimeMillis()) / 1000;
        tokenStoreService.saveRefresh(userId,refresh,ttlSec);

        return new AuthResponse.Token(newAccess,refresh);
    }

    private void loginLogging(Long userId, String email, ActionType actionType, String description, String ipAddress){
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setEmail(email);
        loginLog.setCreatedAt(LocalDateTime.now());
        loginLog.setStatus(actionType);
        loginLog.setDescription(description);
        loginLog.setIpAddress(ipAddress);

        loginLogDAO.save(loginLog);
    }

}
