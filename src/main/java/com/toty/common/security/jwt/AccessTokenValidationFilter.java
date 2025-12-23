package com.toty.common.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 리프레시 토큰따라 entrypoint에서 예외처리 세분화
// 액세스 토큰 유효하면 다음 필터로 넘어가기
// 액세스 토큰이 있는데 만료된 사용자 -> 갱신하도록 리다이렉트
// 보호된 리소스에 익명 사용자 -> 로그인 페이지 redirect
@Component
@RequiredArgsConstructor
public class AccessTokenValidationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 정적 리소스 제외
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/img/") || path.startsWith("/images/") ||
            path.startsWith("/static/") || path.startsWith("/posts/images/") ||
            path.equals("/favicon.ico")) {
            return true;
        }

        // Firebase Service Worker 제외
        if (path.equals("/firebase-messaging-sw.js")) {
            return true;
        }

        // Swagger UI 제외
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return true;
        }

        // 로그인, 회원가입, alert 페이지 제외
        if (path.endsWith("/login") || path.endsWith("/sign-in") ||
            path.endsWith("/signup") || path.contains("/signup") ||
            path.contains("/alert/")) {
            return true;
        }

        // 회원가입 관련 API 제외
        if (path.equals("/api/users/sign-in") || path.equals("/api/users/sign-up") ||
            path.equals("/api/users/signup") || path.equals("/api/users/check-email") ||
            path.equals("/api/users/check-nickname") || path.equals("/api/users/authCode") ||
            path.equals("/api/users/check-authCode")) {
            return true;
        }

        // OAuth2 경로 제외
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        System.out.println("AccessTokenValidationFilter---------");
        String accessToken = "";

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }
            }
        }

        try {
            if (!accessToken.isBlank()) { // 액세스 토큰이 있는데 만료된 사용자 -> 갱신하도록 리다이렉트
                boolean isExpired = jwtTokenUtil.isTokenExpired(accessToken);
            }
        } catch (ExpiredJwtException e) {
            // 액세스 토큰 만료 시 쿠키 삭제
            System.out.println("AccessTokenValidationFilter - 액세스 토큰 만료, 쿠키 삭제 후 로그인 페이지로 리다이렉트");

            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/view/users/login");
            return;
        }

        // 액세스 토큰 유효하면 다음 필터로 넘어가기
        chain.doFilter(request, response);
        }
    }
