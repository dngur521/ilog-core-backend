package com.webkit640.ilog_core_backend.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webkit640.ilog_core_backend.infrastructure.security.LinkTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    private final LinkTokenService linkTokenService;
    private final MinutesService minutesService;
    private final FolderService folderService;

    public String resolveFrontendPath(String uuid, Long userId) {
        String type = linkTokenService.getType(uuid);
        Long id = linkTokenService.getId(uuid);

        if(type == null || id == null) return frontendBaseUrl;

        switch(type) {
            case "MINUTES" ->
                    {
                        if(userId != null){
                            minutesService.joinByInvite(id, userId);
                        }
                        return frontendBaseUrl + "/minutes/" + id;
                    }
            case "FOLDER" -> {
                    if(userId != null){
                        folderService.joinByInvite(id,userId);
                    }
                return frontendBaseUrl + "/folders/" + id;
            }
            default -> {
                return frontendBaseUrl;
            }
        }
    }
}
