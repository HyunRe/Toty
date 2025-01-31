package com.toty.security;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.savedrequest.CookieRequestCache;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 액세스 토큰 만료 후 리프레시 토큰으로 인증 갱신 성공 시 직전 요청 처리 컨트롤러
@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final CookieRequestCache requestCache;

    // 토큰 갱신 시 CookieRequestCache 마지막 요청 경로로 forward
    @GetMapping("/api/auth/refresh")
    public void redirectToSavedRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        requestCache.removeRequest(request, response); // 요청 재사용 방지

        if (savedRequest != null) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(savedRequest.getRedirectUrl());
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect("/home");
        }
    }
}
