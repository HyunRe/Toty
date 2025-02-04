package com.toty.springconfig.security.jwt;

import com.toty.springconfig.redis.RedisService;
import com.toty.springconfig.security.authentication.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.savedrequest.CookieRequestCache;
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
    private final RedisService refreshTokenService;
    private final MyUserDetailsService myUserDetailsService; // todo oauth2user
    private final CookieRequestCache requestCache;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/api/auth/refresh") || path.equals("/login");
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

//        if (accessToken.isBlank()) { // 액세스 토큰 없으면 로그인 페이지 리다이렉트
//            throw new RefreshTokenExpiredException("재로그인이 필요합니다.");
//        }

        try {
            if (!accessToken.isBlank()) { // 액세스 토큰이 있는데 만료된 사용자 -> 갱신하도록 리다이렉트
                boolean isExpired = jwtTokenUtil.isTokenExpired(accessToken);
            }
        } catch (ExpiredJwtException e) {
             throw new AccessTokenExpiredException("액세스 토큰이 만료되었습니다.");
        }

        // 액세스 토큰 유효하면 다음 필터로 넘어가기
        chain.doFilter(request, response);
        }
    }
