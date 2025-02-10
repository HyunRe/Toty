package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class NotificationSenderNotFoundException extends BaseException {
    public NotificationSenderNotFoundException(String type) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, type + " 구현체가 존재하지 않습니다.");
    }
}
