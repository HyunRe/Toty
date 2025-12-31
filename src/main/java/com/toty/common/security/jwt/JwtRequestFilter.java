package com.toty.common.security.jwt;

import com.toty.common.security.authentication.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 로그인할 때 제외하고 security context에 사용자 정보 넣는 로직
// 없으면 null, 있으면 정보 넣기
// * 액세스 토큰 만료 시 익명 사용자로 설정
//  -> AuthenticationEntryPoint에서 (Get /api/auth/refresh) 리다이렉트 구현
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 정적 리소스 제외
        if (path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/img/") || path.startsWith("/images/") ||
            path.startsWith("/static/") || path.startsWith("/posts/images/") ||
            path.equals("/favicon.ico")) {
            return true;
        }

        // Firebase Service Worker 제외
        if (path.equals("/firebase-messaging-sw.js")) {
            return true;
        }

        // Swagger UI 제외
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return true;
        }

        // 인증 관련 경로 제외
        if (path.equals("/api/users/sign-in") || path.equals("/api/users/sign-up") ||
            path.equals("/api/users/signup") || path.equals("/api/auth/refresh") ||
            path.equals("/api/users/check-email") || path.equals("/api/users/check-nickname") ||
            path.equals("/api/users/authCode") || path.equals("/api/users/check-authCode") ||
            path.equals("/api/users/find-email") || path.equals("/api/users/reset-password") ||
            path.equals("/view/users/find-email") || path.equals("/view/users/reset-password") ||
            path.endsWith("/login") || path.endsWith("/signup") || path.endsWith("/sign-in") ||
            path.contains("/signup") || path.contains("/alert/")) {
            return true;
        }

        // OAuth2 경로 제외
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        log.debug("========== JwtRequestFilter.doFilterInternal ==========");
        log.debug("Request URI: {}", request.getRequestURI());

        String username = null;
        String jwt = null;

        String authorization = null;
        Cookie[] cookies = request.getCookies();

        try {
            if (cookies != null) {
                log.debug("Cookies found: {}", cookies.length);
                for (Cookie cookie : cookies) {
                    log.debug("Cookie name: {}", cookie.getName());
                    if (cookie.getName().equals("accessToken")) {
                        authorization = cookie.getValue();
                        log.debug("Access token found in cookie");

                        boolean isExpired = jwtTokenUtil.isTokenExpired(authorization);
                        log.debug("Token expired: {}", isExpired);

                        if (!isExpired) {
                            // 유효한 경우
                            jwt = authorization;
                            username = jwtTokenUtil.extractUsername(jwt);
                            log.debug("Username from token: {}", username);
                        }
                    }
                }
            } else {
                log.debug("No cookies found");
            }

            // contextHolder에 정보 저장하기
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Loading user details for: {}", username);
                UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                // 블랙리스트 체크 포함한 토큰 유효성 검사
                if (jwtTokenUtil.validateTokenWithBlacklist(jwt, userDetails.getUsername())) {
                    log.debug("Token validated, setting authentication");
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(usernamePasswordAuthenticationToken);
                    log.debug("Authentication set in SecurityContext");
                } else {
                    log.warn("Token validation failed or blacklisted - username: {}", username);
                }
            } else {
                if (username == null) {
                    log.debug("Username is null, skipping authentication");
                } else {
                    log.debug("Authentication already exists in SecurityContext");
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.debug("ExpiredJwtException: {}", e.getMessage());
            // 액세스 토큰 만료된 사람 -> 익명 사용자
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Exception in JwtRequestFilter: {}", e.getMessage(), e);
            chain.doFilter(request, response);
        }

    }

}