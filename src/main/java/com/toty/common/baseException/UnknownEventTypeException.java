package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class UnknownEventTypeException extends BaseException {
    public UnknownEventTypeException(String event) {
        super(HttpStatus.BAD_REQUEST, "없는 이벤트 타입: " + event);
    }
}
