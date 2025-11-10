package com.webkit640.ilog_core_backend.api.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.application.service.LinkService;
import com.webkit640.ilog_core_backend.infrastructure.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LinkRedirectController {
    private final LinkService linkService;

    @GetMapping("/redirect/{uuid}")
    public ResponseEntity<Void> redirect(
            @PathVariable("uuid") String uuid,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        if(currentUser == null || currentUser.getId() == null){
            return ResponseEntity.status(302)
                    .location(URI.create("http://localhost:3000/login?next=/redirect/" + uuid))
                    .build();
        }
        Long currentUserId = currentUser.getId();
        String target = linkService.resolveFrontendPath(uuid, currentUserId);

        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }
}