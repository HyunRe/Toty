package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class UnknownCategoryException extends BaseException {
    public UnknownCategoryException(String category) {
        super(HttpStatus.BAD_REQUEST, "없는 카테고리: " + category);
    }
}
