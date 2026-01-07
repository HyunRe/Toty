package com.toty.common.security;

import com.toty.common.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
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

        // 새로 발급하고 response에 넣는 과정
        String accessToken = jwtTokenUtil.generateAccessToken(authentication.getName());
        String refreshToken = jwtTokenUtil.generateRefreshToken(authentication.getName());

        jwtTokenUtil.storeRefreshToken(authentication.getName(), refreshToken);

        // HTTPS 환경에서 Secure 및 SameSite 속성이 적용된 쿠키 생성
        // Servlet 환경에서는 Set-Cookie 헤더를 직접 구성
        String accessTokenCookie = jwtTokenUtil.createSetCookieHeader("accessToken", accessToken, false);
        String refreshTokenCookie = jwtTokenUtil.createSetCookieHeader("refreshToken", refreshToken, true);

        // Set-Cookie 헤더로 쿠키 전송 (SameSite 속성 포함)
        // addHeader를 사용하여 여러 Set-Cookie 헤더 추가
        response.addHeader("Set-Cookie", accessTokenCookie);
        response.addHeader("Set-Cookie", refreshTokenCookie);

        log.info("========== [FormLoginSuccessHandler] 로그인 성공 ==========");
        log.info("사용자: {}", authentication.getName());
        log.info("Access Token Cookie 전체: {}", accessTokenCookie);
        log.info("Refresh Token Cookie 전체: {}", refreshTokenCookie);
        log.info("Set-Cookie 헤더 개수: 2개");
        log.info("리다이렉트 URL: /view/users/home");

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
