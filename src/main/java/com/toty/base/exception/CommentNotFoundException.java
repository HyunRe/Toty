package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends BaseException {
    public CommentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
    }
}
