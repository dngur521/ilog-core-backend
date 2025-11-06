package com.webkit640.ilog_core_backend.infrastructure.security;

import com.webkit640.ilog_core_backend.domain.model.ActionType;
import com.webkit640.ilog_core_backend.domain.model.LoginLog;
import com.webkit640.ilog_core_backend.domain.model.Member;
import com.webkit640.ilog_core_backend.domain.repository.LoginLogDAO;
import com.webkit640.ilog_core_backend.domain.repository.MemberDAO;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customMemberDetailsService;
    private final TokenStoreService tokenStore;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        //Authorization 헤더 추출
        String header = request.getHeader("Authorization");

        // Bearer 토큰인지 확인
        if(header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
            try {
                //유효성 검사
                if(!jwtTokenProvider.isTokenValid(token)) {
                    // 익명으로 계속 진행 (엔드포인트 권한 규칙에 따라 401/403 처리는 SecurityConfig 에서 한다)
                    filterChain.doFilter(request, response);
                    return;
                }

                //블랙리스트 여부 확인
                String jti = jwtTokenProvider.getJti(token);
                if(jti != null && tokenStore.isBlacklisted(jti)){
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                //이미 인증된 경우 중복 인증 방지
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtTokenProvider.getUserId(token);
                //DB 사용자 정보로 항상 최신 권한 셋업
                UserDetails userDetails = customMemberDetailsService.loadUserById(userId);

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                // 요청 정보 추가
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                //SecurityContext에 등록
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if(log.isDebugEnabled()) {
                    String masked = token.length() > 15 ? token.substring(0, 15) + "..." : "short-token";
                    log.debug("JWT accepted for URI: {}, token(masked): {}", request.getRequestURI(), masked);
                }
                filterChain.doFilter(request, response);
            } catch (JwtException e){
                log.warn("JWT exception for URI {} : {}", request.getRequestURI(),e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception e){
                log.error("Unexpected error in JwtAuthenticationFilter", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        }
    }

    @Slf4j
    @Component
    public static class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
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

                    loginLogDAO.save(logEntity);

                    log.info("User {} 자동 로그아웃 처리 (key expired: {})", userId, expiredKey);
                }catch (Exception e){
                    log.error("자동 로그아웃 처리 중 오류 - key: {}", expiredKey, e);
                }
            }
        }
    }
}
