package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class JsonProcessingCustomException extends BaseException {
    public JsonProcessingCustomException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "알림 JSON 변환 오류");
        initCause(cause); // 원본 예외 연결
    }
}
