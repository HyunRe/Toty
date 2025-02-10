package com.toty.springconfig.security;

import static com.toty.springconfig.security.jwt.JwtTokenUtil.ACCESS_TOKEN_TTL;
import static com.toty.springconfig.security.jwt.JwtTokenUtil.REFRESH_TOKEN_TTL;

import com.toty.springconfig.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.CookieRequestCache;
import org.springframework.stereotype.Component;

@Component
public class FormLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    private final JwtTokenUtil jwtTokenUtil;

    public FormLoginSuccessHandler(JwtTokenUtil jwtTokenUtil) {
//        setRequestCache(new CookieRequestCache());
        setDefaultTargetUrl("/api/users/login-success");
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // 새로 발급하고 response에 넣는 과정
        String accessToken = jwtTokenUtil.generateToken(authentication.getName(), ACCESS_TOKEN_TTL);
        String refreshToken = jwtTokenUtil.generateToken(authentication.getName(), REFRESH_TOKEN_TTL);

        jwtTokenUtil.storeRefreshToken(authentication.getName(), refreshToken);

        //응답에 쿠키 포함
        response.addCookie(jwtTokenUtil.createCookie("accessToken", accessToken, false));
        response.addCookie(jwtTokenUtil.createCookie("refreshToken", refreshToken, true));
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
}
