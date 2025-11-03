package com.webkit640.ilog_core_backend.infrastructure.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenStoreService tokenStoreService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService, tokenStoreService);
    }

    //비밀번호 암호화 방식 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        //new BCryptPasswordEncoder(12); <- 강도 조절
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    //cosr설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // cfg.setAllowedOrigins(List.of("https://webkit-ilo9.duckdns.org", "http://localhost:3000", "*"));
        cfg.setAllowedOriginPatterns(List.of("https://webkit-ilo9.duckdns.org", "http://localhost:3000", "*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> response.setStatus(401);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> response.setStatus(403);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) //JWT기반, CSRF 안씀
                .cors(Customizer.withDefaults())
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
                //url 접근 권한
                .exceptionHandling(e -> e
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()))
                .headers(h -> h
                .xssProtection(Customizer.withDefaults())
                .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none; frame-ancestors 'none'")
                )
                // .frameOptions(frame -> frame.sameOrigin()) H2 콘솔 등 필요시
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/members").permitAll()
                .requestMatchers(HttpMethod.POST, "/members/find-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/members/password/verify").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/members/password/reset").permitAll()
                .requestMatchers(HttpMethod.POST, "/jitsi-jwt").permitAll()
                .requestMatchers(HttpMethod.POST, "/summaries/audio").permitAll()
                .requestMatchers(HttpMethod.POST, "/summaries/retry").permitAll()
                .requestMatchers(HttpMethod.POST, "/summaries/simple").permitAll()
                .requestMatchers(HttpMethod.POST, "/rag/index").permitAll()
                .requestMatchers(HttpMethod.POST, "/rag/ask").permitAll()
                .requestMatchers(HttpMethod.GET, "/members").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/members").authenticated()
                .requestMatchers("/members/**").authenticated()
                .requestMatchers("/meetings/**").authenticated()
                .requestMatchers("/minutes/**").authenticated()
                .requestMatchers("/folders/**").authenticated()
                .requestMatchers("/memos/**").authenticated()
                .requestMatchers("/logs/**").authenticated()
                .anyRequest().authenticated()
                )
                //JwtAuthenticationFilter등록
                //UsernamePasswordAuthenticationFilter 전에 실행 되어야 함
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
