package com.webkit640.ilog_core_backend.api.request;

import com.webkit640.ilog_core_backend.domain.model.MinutesType;

import lombok.Data;

public class MinutesRequest {

    @Data
    public static class Create {

        private String title;
        private String content;
        private MinutesType status;
    }

    @Data
    public static class Update {

        private String title;
        private String content;
    }

    @Data
    public static class Delete {
    }
}
