package com.webkit640.ilog_core_backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LinkTokenService {
    private final StringRedisTemplate redis;
    // 기본 리다이렉트 엔드포인트 경로 (상대 경로)
    @Value("${app.link.base-path:/redirect}")
    private String basePath;

    // 링크 만료 (기본 2일)
    @Value("${app.link.ttl-seconds:172800}")
    private Long ttlSeconds;

    public String createLink(String type, Long id) {
        String uuid = UUID.randomUUID().toString();

        String key = "LINK:" + uuid;
        String value = type + ":" + id;

        redis.opsForValue().set(key,value, Duration.ofSeconds(ttlSeconds));
        return basePath + "/" + uuid;
    }

    public String getType(String uuid) {
        String val = redis.opsForValue().get("LINK:" + uuid);
        if(val == null) return null;
        return val.split(":")[0];
    }

    public Long getId(String uuid) {
        String val = redis.opsForValue().get("LINK:" + uuid);
        if(val == null) return null;
        return Long.valueOf(val.split(":")[1]);
    }
}

