package com.webkit640.ilog_core_backend.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/uploads/**" 요청을 서버의 실제 경로로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/home/webkit/uploads/") // 반드시 file: prefix
                .setCachePeriod(3600); // (선택) 캐시 타임
    }
}