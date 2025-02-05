package com.toty.base.exception;

import org.springframework.http.HttpStatus;

// Mento Strategy not found 예외 처리
public class MentoStrategyNotFoundException extends BaseException {

    public MentoStrategyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "멘토 전략 패턴을 찾을 수 없습니다.");
    }
}