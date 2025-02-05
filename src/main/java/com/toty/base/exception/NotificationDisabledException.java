package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class NotificationDisabledException extends BaseException {

    public NotificationDisabledException() {
        super(HttpStatus.FORBIDDEN, "알림이 비활성화 되었습니다.");
    }
}
