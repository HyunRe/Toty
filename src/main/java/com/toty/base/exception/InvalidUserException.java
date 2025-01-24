package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class InvalidUserException extends BaseException {
    public InvalidUserException(Long userId) {
        super(HttpStatus.BAD_REQUEST, "유효 하지 않은 사용자 ID: " + userId);
    }
}