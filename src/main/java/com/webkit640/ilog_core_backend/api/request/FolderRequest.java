package com.webkit640.ilog_core_backend.api.request;

import lombok.Data;

public class FolderRequest {

    @Data
    public static class Create {

        private String folderName;
        private String imageUrl;
    }

    @Data
    public static class Update {

        private String folderName;
        private String imageUrl;
    }

    @Data
    public static class Search {
        private String minutesName;
    }
}
