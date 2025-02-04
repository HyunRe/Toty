package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseException {
    public RateLimitExceededException() {
        super(HttpStatus.TOO_MANY_REQUESTS, "SSE 연결 요청이 너무 많습니다.");
    }
}
