package com.webkit640.ilog_core_backend.infrastructure.util;

import com.webkit640.ilog_core_backend.api.exception.CustomException;
import com.webkit640.ilog_core_backend.api.request.ParticipantRequest;
import com.webkit640.ilog_core_backend.domain.model.ErrorCode;
import com.webkit640.ilog_core_backend.domain.model.Member;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpUtils {
    public static String getClientIP(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if(ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)){
            return ip.split(",")[0]; //첫번째가 실제 클라이언트
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
