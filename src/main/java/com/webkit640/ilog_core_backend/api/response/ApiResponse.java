package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private boolean isSuccess;
    private T data;

    public static <T> ApiResponse<T> success(int code, T data) {
        return new ApiResponse<>(code, true, data);
    }

    public static <T> ApiResponse<T> fail(int code, T data) {
        return new ApiResponse<>(code, false, data);
    }
}
