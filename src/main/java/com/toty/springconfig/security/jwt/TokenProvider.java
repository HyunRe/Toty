package com.toty.springconfig.security.jwt;

import com.toty.springconfig.redis.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

//인증논리 구현하는 부분.
@Component
@RequiredArgsConstructor
public class TokenProvider implements AuthenticationProvider {

    private final JwtTokenUtil jwtTokenUtil;
    private final RedisService redisService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        System.out.println("TokenProvider.authenticate");
        String username = (String) authentication.getPrincipal();
        String refreshToken = (String) authentication.getCredentials();

        // refresh 유효성 확인
        String foundRefreshToken = redisService.getData(username);
        if (foundRefreshToken == null) {
            throw new RefreshTokenExpiredException("db에 "+ username + " 리프레시 토큰 없음");
        }

        try {
            boolean isExpired = jwtTokenUtil.isTokenExpired(foundRefreshToken);
        } catch (ExpiredJwtException ex) {
             throw new RefreshTokenExpiredException("refreshToken 만료됨.");
        }

        if (!foundRefreshToken.equals(refreshToken)) {
            throw new BadCredentialsException("리프레시 토큰 일치하지 않음.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // throw usernamenotfoundException
        return new TokenAuthentication(userDetails, null,
                userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aclass) {
        return TokenAuthentication.class.isAssignableFrom(aclass);
    }
}
