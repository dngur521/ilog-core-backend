package com.webkit640.ilog_core_backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class LinkTokenService {
    @Value("${security.jwt.secret}")
    private String secretBase64;

    // 기본 리다이렉트 엔드포인트 경로 (상대 경로)
    @Value("${app.link.base-path:/r}")
    private String basePath;

    // 링크 만료 (기본 2일)
    @Value("${app.link.ttl-seconds:172800}")
    private Long ttlSeconds;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createLink(String resourceType, Long resourceId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + Math.max(1, ttlSeconds) * 1000);
        String token = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("rt", resourceType) // resource type: FOLDER / MINUTES
                .claim("rid", resourceId)  // resource id
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        return basePath + "/" + token;
    }

    public String getType(String token) {
        Object t = parse(token).get("rt");
        return t != null ? t.toString() : null;
    }

    public Long getId(String token) {
        Object v = parse(token).get("rid");
        return v instanceof Number ? ((Number) v).longValue() : null;
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

