package com.toty.base.response;

import lombok.Getter;

@Getter
public class BaseResponse {
    private final int status;
    private final String message;

    public BaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
