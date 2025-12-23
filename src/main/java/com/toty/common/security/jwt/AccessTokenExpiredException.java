package com.toty.common.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class AccessTokenExpiredException extends AuthenticationException {
    public AccessTokenExpiredException(String msg) {
        super(msg);

    }
}
