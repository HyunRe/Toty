package com.toty.common.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse extends BaseResponse {
    private final List<String> errors;

    public ErrorResponse(int status, String message, List<String> errors) {
        super(status, message);
        this.errors = errors;
    }
}
