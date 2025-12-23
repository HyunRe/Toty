package com.toty.common.baseException;

import com.toty.notification.domain.type.EventType;
import org.springframework.http.HttpStatus;

public class UnSupportedNotificationTypeException extends BaseException {
    public UnSupportedNotificationTypeException(EventType eventType) {
        super(HttpStatus.BAD_REQUEST, "지원 되지 않는 알림 유형: " + eventType);
    }
}