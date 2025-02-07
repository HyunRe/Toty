package com.toty.base.exception;

import org.springframework.http.HttpStatus;

public class PostCategoryCreationNotSupportedException extends BaseException {
    public PostCategoryCreationNotSupportedException() {
        super(HttpStatus.BAD_REQUEST, "해당 카테고리에 대한 게시글 생성 전략이 존재하지 않습니다.");
    }
}
