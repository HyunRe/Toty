package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class EmailNotRegisteredException extends BaseException {
    public EmailNotRegisteredException() {
        super(HttpStatus.BAD_REQUEST, "이메일 미등록 사용자입니다.");
    }
}
