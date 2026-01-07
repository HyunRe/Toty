package com.toty.common.security;

import com.toty.common.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
        ResponseCookie accessTokenCookie = jwtTokenUtil.createSecureResponseCookie("accessToken", accessToken, false);
        ResponseCookie refreshTokenCookie = jwtTokenUtil.createSecureResponseCookie("refreshToken", refreshToken, true);

        // Set-Cookie 헤더로 쿠키 전송 (SameSite 속성 포함)
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
