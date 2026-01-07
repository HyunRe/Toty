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

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(isSecure)     // ⭐ 중요
                .path("/")            // ⭐ 반드시
                .sameSite("Lax")
                .maxAge(60 * 30)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isSecure)     // ⭐ 중요
                .path("/")            // ⭐ 반드시
                .sameSite("Lax")
                .maxAge(60 * 60 * 24 * 14)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("로그인 성공: {}", authentication.getName());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
