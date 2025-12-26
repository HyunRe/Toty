package com.toty.common.security;

import com.toty.common.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        //응답에 쿠키 포함
        response.addCookie(jwtTokenUtil.createCookie("accessToken", accessToken, false));
        response.addCookie(jwtTokenUtil.createCookie("refreshToken", refreshToken, true));
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
