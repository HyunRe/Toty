package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class UnSupportedNotificationTypeException extends BaseException {
    public UnSupportedNotificationTypeException(String type) {
        super(HttpStatus.BAD_REQUEST, "지원 되지 않는 알림 유형: " + type);
    }
}
