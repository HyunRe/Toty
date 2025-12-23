package com.toty.common.response;

import lombok.Getter;

@Getter
public class SuccessResponse extends BaseResponse {
    private final Object data; // 성공 응답 시 포함할 데이터

    public SuccessResponse(int status, String message, Object data) {
        super(status, message);
        this.data = data;
    }
}
