package com.webkit640.ilog_core_backend.application.service;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.MinutesRequest;
import com.webkit640.ilog_core_backend.api.response.MinutesResponse;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import com.webkit640.ilog_core_backend.domain.repository.MinutesLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinutesLockService {
    private final MinutesLockRepository lockRepository;
    private final StringRedisTemplate redis;
    private final MemberDAO memberDAO;
    private static final long TTL_SECONDS = 30;

    //토큰 발급
    public String acquire(Long minutesId, Long userId){
        String current = lockRepository.getLock(minutesId);

        if(current != null){
            throw new CustomException(ErrorCode.LOCK_DENIED);
        }

        String token = UUID.randomUUID().toString();

        lockRepository.setLock(minutesId,token,TTL_SECONDS);

        String keyUser = "LOCK:MINUTES:" + minutesId + ":USER";
        redis.opsForValue().set(keyUser, userId.toString());
        redis.expire(keyUser, Duration.ofSeconds(TTL_SECONDS));

        return token;
    }

    //토큰 검증
    public void validate(Long minutesId, String token){
        String stored = lockRepository.getLock(minutesId);

        if(stored == null || !stored.equals(token)){
            throw new CustomException(ErrorCode.LOCK_DENIED);
        }
    }

    //토큰 신선도 유지
    public void refresh(Long minutesId, MinutesRequest.Lock request){
        lockRepository.refresh(minutesId, request.getToken(),TTL_SECONDS);

        String keyUser = "LOCK:MINUTES:"+ minutesId + ":USER";
        redis.expire(keyUser, Duration.ofSeconds(TTL_SECONDS));
    }

    //토큰 만료
    public void release(Long minutesId, MinutesRequest.Lock request){
        lockRepository.release(minutesId, request.getToken());

        String keyUser = "LOCK:MINUTES:" + minutesId + ":USER";
        redis.delete(keyUser);
    }

    //------------ lock 상태 조회 --------------
    public MinutesResponse.LockStatus getLockStatus(Long minutesId) {

        String key = "LOCK:MINUTES:" + minutesId;
        String token = redis.opsForValue().get(key);

        if(token == null){
            return new MinutesResponse.LockStatus(false,null,null,null);
        }
        Long ttl = redis.getExpire(key);

        String userIdStr = redis.opsForValue().get(key + ":USER");
        Long userId = userIdStr != null ? Long.valueOf(userIdStr) : null;

        String userName = null;

        if(userId != null){
            Member user = memberDAO.findById(userId).orElse(null);
            if(user != null) userName = user.getName();
        }

        return new MinutesResponse.LockStatus(true,ttl,userId, userName);
    }

}
