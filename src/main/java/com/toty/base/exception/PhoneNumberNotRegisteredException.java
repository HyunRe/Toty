package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class PhoneNumberNotRegisteredException extends BaseException {
    public PhoneNumberNotRegisteredException() {
        super(HttpStatus.BAD_REQUEST, "전화번호 미등록 회원입니다.");
    }
}
