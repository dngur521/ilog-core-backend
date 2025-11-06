package com.webkit640.ilog_core_backend.infrastructure.security;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.webkit640.ilog_core_backend.domain.repository.LoginLogDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final LoginLogDAO loginLogDAO;
    private final TokenStoreService tokenStoreService;

    // 비밀키
    @Value("${security.jwt.secret}")
    private String secretBase64;

    @Value("${security.jwt.access-expiration-ms:18000000}")
    private Long accessExpirationMs;

    @Value("${security.jwt.refresh-expiration-ms:604800000}")
    private Long refreshExpirationMs;

    // 기본값 (발급자 스푸핑 방지 목적)
    @Value("${security.jwt.issuer:team-lck}")
    private String issuer;

    //기본 30초 관용치
    @Value("${security.jwt.clock-skew-seconds:30}")
    private Long clockSkewSeconds;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId,List<String> roles) {
        return buildToken(userId, roles, "ACCESS", accessExpirationMs);
    }

    public String createRefreshToken(Long userId) {
        return buildToken(userId, List.of(), "REFRESH", refreshExpirationMs);
    }

    //토큰 생성
    public String buildToken(Long userId, List<String> roles, String type, Long ttLms) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttLms);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("id", userId)
                .claim("roles", roles)
                .claim("type", type)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public String getType(String token) {
        Object t = parseClaims(token).get("type");
        return t != null ? t.toString() : null;
    }

    public boolean isTokenValid(String token) {
        try {
            // clockSkewSeconds 만큼 시계오차 허용
            Jwts.parserBuilder()
                    .requireIssuer(issuer) //발급자 일치 검사
                    .setAllowedClockSkewSeconds(clockSkewSeconds)
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            //만료된 토큰
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            //서명 위조, 형식 오류 등
            return false;
        }
    }

    //Claims 파싱 (검증 포함)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //토큰에서 userId 추출
    public Long getUserId(String token) {
        Object id = parseClaims(token).get("id");
        return id instanceof Number ? ((Number) id).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        return roles instanceof List ? (List<String>) roles : List.of();
    }
}
