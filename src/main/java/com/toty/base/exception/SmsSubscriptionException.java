package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class SmsSubscriptionException extends BaseException {
    public SmsSubscriptionException() {
        super(HttpStatus.BAD_REQUEST, "문자 수신 미동의 회원입니다.");
    }
}
