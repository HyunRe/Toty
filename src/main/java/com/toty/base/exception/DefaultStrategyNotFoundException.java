package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class DefaultStrategyNotFoundException extends BaseException {

    public DefaultStrategyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "기본 전략 패턴을 찾을 수 없습니다.");
    }
}