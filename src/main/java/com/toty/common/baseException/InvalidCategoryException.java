package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class InvalidCategoryException extends BaseException {
    public InvalidCategoryException(String postCategory) {
        super(HttpStatus.BAD_REQUEST, "유효 하지 않은 카테고리: " + postCategory);
    }
}