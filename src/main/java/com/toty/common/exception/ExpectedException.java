package com.toty.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExpectedException extends RuntimeException {
<<<<<<< HEAD
=======

>>>>>>> origin/develop
    private final ErrorCode errorCode;
}
