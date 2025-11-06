package com.webkit640.ilog_core_backend.application.service;

import org.springframework.stereotype.Service;

import com.webkit640.ilog_core_backend.infrastructure.security.LinkTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkTokenService linkTokenService;

    public String resolveFrontendPath(String token, String frontendBaseUrl) {
        String type = linkTokenService.getType(token);
        Long id = linkTokenService.getId(token);

        if(type == null || id == null) return frontendBaseUrl;

        return switch(type) {
            case "MINUTES" -> frontendBaseUrl + "/app/minutes/" + id;
            case "FOLDER" -> frontendBaseUrl + "/app/folders/" + id;
            default -> frontendBaseUrl;
        };
    }
}
