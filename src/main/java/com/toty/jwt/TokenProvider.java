package com.toty.jwt;

import com.toty.redisConfig.RedisService;
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

        // refresh 유효한지 확인
        String foundRefreshToken = redisService.getData(username);
//        String foundRefreshToken = redisService.getData("username");
        if (foundRefreshToken == null) {
            throw new RefreshTokenExpiredException("refreshToken 만료됨.");
        }
        if (!foundRefreshToken.equals(refreshToken)) {
            throw new BadCredentialsException("refreshToken이 일치하지 않음.");
        }

        try {
            boolean isExpired = jwtTokenUtil.isTokenExpired(foundRefreshToken);
        } catch (ExpiredJwtException ex) {
            // todo
            // throw new RefreshTokenExpiredException("refreshToken 만료됨.");
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
