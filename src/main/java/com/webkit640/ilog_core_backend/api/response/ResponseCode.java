package com.webkit640.ilog_core_backend.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    OK(200,"Ok"), Created(201,"Created");

    private final int code;
    private final String msg;
}
