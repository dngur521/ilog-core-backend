package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

public class LinkResponse {
    @Data
    @AllArgsConstructor
    public static class Share {
        private String link;
    }
}
