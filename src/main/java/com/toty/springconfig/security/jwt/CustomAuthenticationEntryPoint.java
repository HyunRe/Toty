package com.toty.springconfig.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof AccessTokenExpiredException) {
            // 액세스 토큰 만료 시 리프레시 토큰 검증하는 redirect 경로
            System.out.println("CustomAuthenticationEntryPoint(리프레시 토큰 갱신 필요 시) ------------ ");
            response.sendRedirect("/api/auth/refresh");
        } else {
            // 리프레시 토큰 만료 시 로그인 페이지 이동
            System.out.println("CustomAuthenticationEntryPoint(로그인 필요 시) ------------ ");
            response.sendRedirect("/view/users/login");
        }
    }
}