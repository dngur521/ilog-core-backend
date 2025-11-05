package com.webkit640.ilog_core_backend.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenStoreService {

    private final StringRedisTemplate redis;

    //SHA-1 해시
    private String sha1(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for(byte b : digest){
                sb.append(String.format("%02x",b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e){
            throw new CustomException(ErrorCode.ALGORITHM_NOT_AVAILABLE);
        }
    }

    public void blacklistAccess(String jti, long ttlSeconds) {
        String hashedKey = "BLACKLIST:" + sha1(jti);
        redis.opsForValue().set(hashedKey, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        String hashedKey = "BLACKLIST:" + sha1(jti);
        return Boolean.TRUE.equals(redis.hasKey(hashedKey));
    }

    // refresh token 저장
    public void saveRefresh(Long userId, String refresh, Long ttlSeconds) {
        String hashedKey = "REFRESH:" + sha1(String.valueOf(userId));
        redis.opsForValue().set(hashedKey, refresh, Duration.ofSeconds(ttlSeconds));
    }

    public String getRefresh(Long userId) {
        String hashedKey = "REFRESH:" + sha1(String.valueOf(userId));
        return redis.opsForValue().get(hashedKey);
    }

    public void deleteRefresh(Long userId) {
        String hashedKey = "REFRESH:" + sha1(String.valueOf(userId));
        redis.delete(hashedKey);
    }
}
