package com.webkit640.ilog_core_backend.infrastructure.security;

import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.LoginLog;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.LoginLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final LoginLogDAO loginLogDAO;
    private final MemberDAO memberDAO;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      LoginLogDAO loginLogDAO,
                                      MemberDAO memberDAO) {
        super(listenerContainer);
        this.loginLogDAO = loginLogDAO;
        this.memberDAO = memberDAO;
    }

    @Override
    public void onMessage(Message message, byte[] pattern){
        String expiredKey = message.toString();
        if(expiredKey.startsWith("REFRESH:")){
            try{
                Long userId = Long.valueOf(expiredKey.split(":")[1]);

                String email = memberDAO.findById(userId)
                        .map(Member::getEmail)
                        .orElse("unknown");

                LoginLog logEntity = new LoginLog();
                logEntity.setUserId(userId);
                logEntity.setEmail(email);
                logEntity.setCreatedAt(LocalDateTime.now());
                logEntity.setStatus(ActionType.LOGOUT);
                logEntity.setDescription("자동 로그아웃");
                logEntity.setIpAddress(null);

                loginLogDAO.save(logEntity);

                log.info("User {} 자동 로그아웃 처리 (key expired: {})", userId, expiredKey);
            }catch (Exception e){
                log.error("자동 로그아웃 처리 중 오류 - key: {}", expiredKey, e);
            }
        }
    }
}
