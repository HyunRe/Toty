package com.toty.springconfig.security.jwt;

import static com.toty.springconfig.security.jwt.JwtTokenUtil.ACCESS_TOKEN_TTL;
import static com.toty.springconfig.security.jwt.JwtTokenUtil.REFRESH_TOKEN_TTL;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.CookieRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;


// 액세스 토큰 만료 시 리프레시 토큰 갱신 필터
@Component
public class RefreshTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtTokenUtil jwtTokenUtil;

    private String username;

    public RefreshTokenAuthenticationFilter(TokenProvider authenticationManager, JwtTokenUtil jwtTokenUtil, SimpleUrlAuthenticationFailureHandler failurehandler) {
        super(new AntPathRequestMatcher("/api/auth/refresh", "GET")); // 특정 요청만 필터링
        setAuthenticationManager(new ProviderManager(authenticationManager));

        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setRequestCache(new CookieRequestCache());
        setAuthenticationSuccessHandler(successHandler);

        setAuthenticationFailureHandler(failurehandler);
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        // 쿠키에 있는 리프레시 + 인증 토큰
        // 인증토큰 -> 만료 확인, username까지 있는거 확인 -> refresh를 token으로 해서 넣기
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken.isBlank()) {
            throw new RefreshTokenExpiredException("쿠키에 저장된 리프레시 토큰 없음");
        }

        username = jwtTokenUtil.extractUsername(refreshToken);
        System.out.println("username = " + username);

        // 리프레시 토큰 유효성 검사
        System.out.println("RefreshTokenAuthenticationFilter.attemptAuthentication");
        return getAuthenticationManager().authenticate(new TokenAuthentication(username, refreshToken));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        System.out.println("--------------requestCache 내 내용---------------");

        // 새로 발급하고 response에 넣는 과정
        String newAccessToken = jwtTokenUtil.generateToken(username, ACCESS_TOKEN_TTL);
        String newRefreshToken = jwtTokenUtil.generateToken(username, REFRESH_TOKEN_TTL);

        jwtTokenUtil.storeRefreshToken(username, newRefreshToken);

        //응답에 쿠키 포함
        response.addCookie(jwtTokenUtil.createCookie("accessToken", newAccessToken, false));
        response.addCookie(jwtTokenUtil.createCookie("refreshToken", newRefreshToken, true));
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
