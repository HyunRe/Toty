package com.toty.common.security;

import com.toty.common.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FormLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final JwtTokenUtil jwtTokenUtil;

    public FormLoginSuccessHandler(JwtTokenUtil jwtTokenUtil) {
        setDefaultTargetUrl("/view/users/home");
        setAlwaysUseDefaultTargetUrl(true);
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        String accessToken = jwtTokenUtil.generateAccessToken(authentication.getName());
        String refreshToken = jwtTokenUtil.generateRefreshToken(authentication.getName());

        jwtTokenUtil.storeRefreshToken(authentication.getName(), refreshToken);

        boolean isSecure = "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        // AccessToken: JavaScript에서 읽을 수 있어야 함 (httpOnly=false)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(false)      // ⭐ JavaScript 접근 허용 (API 요청 시 필요)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(60 * 30)      // 30분
                .build();

        // RefreshToken: 보안을 위해 httpOnly=true, path=/api/auth
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)       // ⭐ JavaScript 접근 차단 (보안)
                .secure(isSecure)
                .path("/api/auth")    // ⭐ /api/auth 경로에서만 전송
                .sameSite("Lax")
                .maxAge(60 * 60 * 24 * 14)  // 14일
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("========== 로그인 성공 ==========");
        log.info("사용자: {}", authentication.getName());
        log.info("X-Forwarded-Proto: {}", request.getHeader("X-Forwarded-Proto"));
        log.info("isSecure: {}", isSecure);
        log.info("AccessToken httpOnly: false (JavaScript 접근 가능)");
        log.info("RefreshToken httpOnly: true, path: /api/auth");
        log.info("리다이렉트: /view/users/home");

        response.sendRedirect("/view/users/home");
    }
}
