package com.toty.springconfig.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class AccessTokenExpiredException extends AuthenticationException {
    public AccessTokenExpiredException(String msg) {
        super(msg);

    }
}
