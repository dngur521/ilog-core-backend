package com.webkit640.ilog_core_backend.api.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.webkit640.ilog_core_backend.application.service.LinkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LinkRedirectController {
    private final LinkService linkService;
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;
    
    @GetMapping("/redirect/{token}")
    public ResponseEntity<Void> redirect(@PathVariable String token) {
        String target = linkService.resolveFrontendPath(token, frontendBaseUrl);

        return ResponseEntity.status(302)
                .location(URI.create(target))
                .build();
    }
}

