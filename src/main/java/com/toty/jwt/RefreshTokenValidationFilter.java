package com.toty.jwt;

import static com.toty.jwt.JwtTokenUtil.ACCESS_TOKEN_TTL;
import static com.toty.jwt.JwtTokenUtil.REFRESH_TOKEN_TTL;
import com.toty.redisConfig.RedisService;
import com.toty.security.authentication.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// /api/auth/refresh 경로에서만 해당 필터 실행
// * 액세스 토큰 만료 시 리프레시 토큰 검증
// 리프레시 토큰 유효하면 (액세스 + 리프레시) 토큰 재발행 + 레디스 저장 + security context에 정보 저장
// 리프레시 토큰 유효하지 않으면
// -> Authentication EntryPoint 에서 로그인 유도 -> 로그인 성공 시 CookieCache 저장된 마지막 요청 경로로 forward

@Component
@RequiredArgsConstructor
public class RefreshTokenValidationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisService refreshTokenService;
    private final MyUserDetailsService myUserDetailsService; // todo oauth2user



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return !path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        String accessToken = "";
        String refreshToken = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("accessToken")) {
                accessToken = cookie.getValue();
            } else if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }

        if (accessToken.isBlank() || refreshToken.isBlank()) {
            throw new RefreshTokenExpiredException("재로그인이 필요합니다.");
        }

        String username = jwtTokenUtil.extractUsername(accessToken);

        // 레디스에서 username(email)으로 refreshToken 찾기
        String foundRefreshToken = refreshTokenService.getData(username);
        if (!foundRefreshToken.equals(refreshToken) || jwtTokenUtil.isTokenExpired(refreshToken)) {
            // 동일하지 않다면(토큰 탈취), 없다면(리프레시 토큰 만료) -> 재로그인하도록 응답
            throw new RefreshTokenExpiredException("재로그인이 필요합니다.");
        } else {
            // 기존 것과 비교하고 동일하면 access, refresh 두 가지 모두 새로 발급
            String newAccessToken = jwtTokenUtil.generateToken(username, ACCESS_TOKEN_TTL);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(REFRESH_TOKEN_TTL);

            refreshTokenService.setData(username, newRefreshToken, Duration.ofMillis(REFRESH_TOKEN_TTL));

            //응답에 쿠키 포함
            response.addCookie(jwtTokenUtil.createCookie("accessToken", newAccessToken, false));
            response.addCookie(jwtTokenUtil.createCookie("refreshToken", newRefreshToken, true));

            // contextHolder에 정보 저장하기
            // todo JwtRequestFilter와 겹치는 로직 -> 리팩토링 필요?
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(newAccessToken, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            chain.doFilter(request, response);
        }
    }
}
