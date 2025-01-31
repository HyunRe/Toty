package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
    }
}