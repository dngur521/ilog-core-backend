package com.webkit640.ilog_core_backend.infrastructure.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenStoreService {

    private final StringRedisTemplate redis;

    public void blacklistAccess(String jti, long ttlSeconds) {
        String hashedKey = "BLACKLIST:" + jti;
        redis.opsForValue().set(hashedKey, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        String hashedKey = "BLACKLIST:" + jti;
        return Boolean.TRUE.equals(redis.hasKey(hashedKey));
    }

    // refresh token 저장
    public void saveRefresh(Long userId, String refresh, Long ttlSeconds) {
        String key = "REFRESH:" + userId;
        redis.opsForValue().set(key, refresh, Duration.ofSeconds(ttlSeconds));
    }
    // refresh token 저장 (키에 userId 직접 표기)


    public String getRefresh(Long userId) {
        String key = "REFRESH:" + userId;
        return redis.opsForValue().get(key);
    }

    public void deleteRefresh(Long userId) {
        String key = "REFRESH:" + userId;
        redis.delete(key);
    }
}
