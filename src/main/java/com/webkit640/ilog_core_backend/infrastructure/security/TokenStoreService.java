package com.webkit640.ilog_core_backend.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenStoreService {
    private final StringRedisTemplate redis;

    public void blacklistAccess(String jti, long ttlSeconds){
        String key = "BLACKLIST:" + jti;
        redis.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti){
        String key = "BLACKLIST:" + jti;
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    //단일 디바이스 기준: 사용자별 하나의 리프레스만 보관
    public void saveRefresh(Long userId, String refresh, Long ttlSeconds){
        String key = "REFRESH:" + userId;
        redis.opsForValue().set(key, refresh, Duration.ofSeconds(ttlSeconds));
    }
    public String getRefresh(Long userId){
        return redis.opsForValue().get("REFRESH:" + userId);
    }
    public void deleteRefresh(Long userId){
        redis.delete("REFRESH:" + userId);
    }
}
