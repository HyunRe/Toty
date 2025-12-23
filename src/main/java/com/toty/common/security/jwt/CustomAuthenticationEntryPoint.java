package com.toty.common.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // SSE 요청이거나 비동기 요청인 경우 redirect 대신 에러 응답 반환
        String accept = request.getHeader("Accept");
        if ((accept != null && accept.contains("text/event-stream")) || request.isAsyncStarted()) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"인증이 필요합니다.\"}");
                response.getWriter().flush();
            }
            return;
        }

        // 일반 요청인 경우 기존 처리
        if (!response.isCommitted()) {
            if (authException instanceof AccessTokenExpiredException) {
                // 액세스 토큰 만료 시 리프레시 토큰 검증하는 redirect 경로
                System.out.println("CustomAuthenticationEntryPoint(리프레시 토큰 갱신 필요 시) ------------ ");
                response.sendRedirect("/view/users/login");
            } else {
                // 리프레시 토큰 만료 시 로그인 페이지 이동
                System.out.println("CustomAuthenticationEntryPoint(로그인 필요 시) ------------ ");
                response.sendRedirect("/view/users/login");
            }
        }
    }
}