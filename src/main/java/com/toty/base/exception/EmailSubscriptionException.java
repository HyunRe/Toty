package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class EmailSubscriptionException extends BaseException {
    public EmailSubscriptionException() {
        super(HttpStatus.BAD_REQUEST, "이메일 수신 미동의 회원입니다.");
    }
}