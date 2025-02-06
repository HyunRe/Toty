package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class UnreadStrategyNotFoundException extends BaseException {

    public UnreadStrategyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "읽지 않은 알림 전략 패턴을 찾을 수 없습니다.");
    }
}