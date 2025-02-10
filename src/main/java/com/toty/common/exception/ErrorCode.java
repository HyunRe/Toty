package com.toty.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

    INVALID_SEARCH_FIELD(HttpStatus.BAD_REQUEST, "잘못된 검색 속성입니다.");




    private final HttpStatus httpStatus;
    private final String message;
}