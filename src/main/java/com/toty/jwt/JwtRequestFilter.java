package com.toty.jwt;
import com.toty.security.authentication.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 로그인할 때 제외하고 security context에 사용자 정보 넣는 로직
// 없으면 null, 있으면 정보 넣기
// * 액세스 토큰 만료 시간 확인 후 지났으면 Authentication exception 던짐
//  -> AuthenticationEntryPoint에서 (Get /api/auth/refresh) 리다이렉트 구현
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MyUserDetailsService myUserDetailsService; // todo oauth2user

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/users/sign-in") || path.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
//        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        String authorization = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    authorization = cookie.getValue();

                    // * 만료된 경우 (클라이언트에 리프레시 토큰 요청)
                    if (jwtTokenUtil.isTokenExpired(authorization)) {
                        // 기존 api를 쿠키에 저장하고 (Get /api/auth/refresh) 리다이렉트
                        throw new AccessTokenExpiredException(
                                "Access token이 만료되었습니다. Refresh token으로 인증을 갱신하세요");
                    }

                    // 유효한 경우
                    response.setStatus(HttpServletResponse.SC_OK);
                    jwt = authorization;
                    username = jwtTokenUtil.extractUsername(jwt);
                }
            }
        }

        // contextHolder에 정보 저장하기
        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwt, userDetails.getUsername())) {
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