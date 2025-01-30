package com.toty.security;

import static com.toty.jwt.JwtTokenUtil.ACCESS_TOKEN_TTL;
import static com.toty.jwt.JwtTokenUtil.REFRESH_TOKEN_TTL;

import com.toty.jwt.JwtTokenUtil;
import com.toty.redisConfig.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

// * 리프레시 토큰 추가하면서 createCookie 메서드 파라미터 추가, 리프레시 토큰 발행 후 레디스 저장
@RequiredArgsConstructor
@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        System.out.println("AuthSuccessHandler.onAuthenticationSuccess");
        // * accessToken, refreshToken 발급
        String accessToken = jwtTokenUtil.generateToken(authentication.getName(), ACCESS_TOKEN_TTL);
        String refreshToken = jwtTokenUtil.generateRefreshToken(REFRESH_TOKEN_TTL);

        // * 헤더에 쿠키를 포함해 클라이언트에게 전달
        response.addCookie(jwtTokenUtil.createCookie("accessToken", accessToken, false));
        response.addCookie(jwtTokenUtil.createCookie("refreshToken", refreshToken, true));

        // * 리프레시 토큰 레디스 저장 (key: value -> 사용자 이메일 : 리프레시 토큰)
        refreshTokenService.setData(authentication.getName(), refreshToken, Duration.ofMillis(REFRESH_TOKEN_TTL));

        // forward
        request.setAttribute("msg", "로그인에 성공했습니다.");
        request.setAttribute("url", "/home");
        request.getRequestDispatcher("/alert").forward(request, response);
    }


}