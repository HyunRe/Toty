package com.toty.common.security.jwt;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.security.authentication.AccountAdapter;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 정적 리소스
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
                path.startsWith("/img/") || path.startsWith("/images/") ||
                path.startsWith("/static/") || path.startsWith("/posts/images/") ||
                path.equals("/favicon.ico")) {
            return true;
        }

        // Firebase Service Worker
        if (path.equals("/firebase-messaging-sw.js")) {
            return true;
        }

        // Swagger
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return true;
        }

        // 로그인 / 회원가입 / alert
        if (path.endsWith("/login") || path.endsWith("/sign-in") ||
                path.endsWith("/signup") || path.contains("/signup") ||
                path.contains("/alert/")) {
            return true;
        }

        // 회원가입 관련 API
        if (path.equals("/api/users/sign-in") ||
                path.equals("/api/users/sign-up") ||
                path.equals("/api/users/signup") ||
                path.equals("/api/users/check-email") ||
                path.equals("/api/users/check-nickname") ||
                path.equals("/api/users/authCode") ||
                path.equals("/api/users/check-authCode")) {
            return true;
        }

        // OAuth2
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        System.out.println("AccessTokenValidationFilter---------");

        String accessToken = null;

        // ✅ 1. 쿠키에서 AccessToken 읽기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        // 토큰 없으면 그냥 다음 필터
        if (accessToken == null || accessToken.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // ✅ 2. 토큰 만료 검사
            if (jwtTokenUtil.isTokenExpired(accessToken)) {
                throw new ExpiredJwtException(null, null, "Access token expired");
            }

            // ✅ 3. 토큰에서 사용자 정보 추출
            String email = jwtTokenUtil.extractUsername(accessToken);

            // 이미 인증된 경우는 패스
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ 1. 토큰에서 추출한 username(email)로 DB 조회
                User user = userRepository.findByEmail(email).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

                // ✅ 2. AccountAdapter로 UserDetails 생성
                AccountAdapter userDetails = new AccountAdapter(user);

                // ✅ 3. Authentication 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 🔥 4. SecurityContext 등록 (가장 중요)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            System.out.println("AccessToken 만료 → 쿠키 삭제 후 로그인 페이지로 이동");

            // AccessToken 쿠키 삭제
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            // RefreshToken 쿠키 삭제
            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            response.sendRedirect("/view/users/login");
            return;
        }

        // ✅ 5. 다음 필터로 진행
        chain.doFilter(request, response);
    }
}

