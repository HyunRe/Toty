package com.toty.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // SSE 요청인 경우 forward 대신 에러 응답 반환 (응답이 이미 커밋된 경우 방지)
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"접근 권한이 없습니다.\"}");
                response.getWriter().flush();
            }
            return;
        }

        // 일반 요청인 경우 기존 처리
        if (!response.isCommitted()) {
            request.setAttribute("msg", "접근 권한이 없습니다.");
            request.getRequestDispatcher("/view/users/alert").forward(request, response);
        }
    }
}