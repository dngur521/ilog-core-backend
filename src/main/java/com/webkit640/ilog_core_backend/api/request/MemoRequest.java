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
    }

    @Data
    public static class Update {
        private Long updateId;
        private String content;
        @NotBlank
        private MemoType memoType;
    }

    @Data
    public static class Delete {
        private Long deleteId;
    }
}
