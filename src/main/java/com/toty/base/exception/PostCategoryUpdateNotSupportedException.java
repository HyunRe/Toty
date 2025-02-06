package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class PostCategoryUpdateNotSupportedException extends BaseException {

    public PostCategoryUpdateNotSupportedException() {
        super(HttpStatus.BAD_REQUEST, "해당 카테고리에 대한 게시글 수정 전략이 존재하지 않습니다.");
    }
}
