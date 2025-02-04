package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends BaseException {
    public NotificationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다.");
    }
}

