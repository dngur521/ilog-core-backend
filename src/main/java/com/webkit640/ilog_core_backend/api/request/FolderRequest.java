package com.webkit640.ilog_core_backend.api.request;

import com.webkit640.ilog_core_backend.domain.model.OrderType;
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

    @Data
    public static class Order {
        private OrderType order = OrderType.APPROACHED_AT_ASC;
    }
}
