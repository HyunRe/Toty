package com.toty.base.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;     // HTTP 상태 코드
    private final String message;        // 예외 메시지

    public BaseException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
