package com.webkit640.ilog_core_backend.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class MinutesLockRepository {
    private final StringRedisTemplate redis;

    private String Key(Long minutesId){
        return "LOCK:MINUTES:" + minutesId;
    }

    public void setLock(Long minutesId, String token, long ttlSeconds){
        redis.opsForValue().set(Key(minutesId), token, Duration.ofSeconds(ttlSeconds));
    }

    public String getLock(Long minutesId){
        return redis.opsForValue().get(Key(minutesId));
    }

    public void refresh(Long minutesId, String token, long ttlSeconds){
        String current = getLock(minutesId);
        if(current != null && current.equals(token)){
            setLock(minutesId,token,ttlSeconds);
        }
    }

    public void release(Long minutesId, String token){
        String current = getLock(minutesId);
        if(current != null && current.equals(token)){
            redis.delete(Key(minutesId));
        }
    }
}
