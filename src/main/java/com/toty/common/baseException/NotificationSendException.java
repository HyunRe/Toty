package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class NotificationSendException extends BaseException {
    public NotificationSendException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송 중 오류가 발생했습니다.");
        initCause(cause); // 원본 예외 연결
    }
}
