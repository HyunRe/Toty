package com.toty.common.baseException;

import org.springframework.http.HttpStatus;

public class UnknownTagException extends BaseException {

    public UnknownTagException(String tag) {
        super(HttpStatus.BAD_REQUEST, "없는 태그: " + tag);
    }
}