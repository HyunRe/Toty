package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedNotificationTypeException extends BaseException {
    public UnsupportedNotificationTypeException(String type) {
        super(HttpStatus.BAD_REQUEST, "지원 되지 않는 알림 유형: " + type);
    }
}
