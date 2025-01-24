package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class PostNotFoundException extends BaseException {
    public PostNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다.");
    }
}
