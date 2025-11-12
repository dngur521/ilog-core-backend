package com.webkit640.ilog_core_backend.api.request;

import com.webkit640.ilog_core_backend.domain.model.MemoType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class MemoRequest {
    @Data
    public static class Create{
        private String content;
        @NotBlank
        private MemoType memoType;

        private int startIndex;
        private int endIndex;
        private String positionContent;
    }

    @Data
    public static class Update {
        private Long id;
        private String content;
        @NotBlank
        private MemoType memoType;

        private Integer startIndex;
        private Integer endIndex;
        private String positionContent;
    }

    @Data
    public static class Delete {
        private Long deleteId;
    }
}
