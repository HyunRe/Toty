package com.toty.springconfig.security.jwt;

import com.toty.springconfig.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// * 리프레시 토큰 발행 메서드 추가, createCookie 메서드 jwtRequestFilter에서 여기로 옮겨옴
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private String SECRET_KEY = "BESP2JupiterELASTICSEARCHPROJECTWOWOWOWOWOWOWOWOWOWOWO";
    private final RedisService redisService;
    // * TTL 설정
    public static final long ACCESS_TOKEN_TTL = 1000*60*60*2; // 액세스 토큰 수명 2시간
    public static final long REFRESH_TOKEN_TTL = 1000*60*60*24*14; // 액세스 토큰 수명 2주

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰에서 사용자 이름 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰에서 만료시간 추출
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 토큰 만료여부 확인
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    // 토큰 생성 로직
    private String createToken(Map<String, Object> claims, String subject, long TTL) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TTL))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 삭제 예정 -> 만료된 액세스 토큰에서 사용자 이름 추출 불가능함
    private String createRefreshToken(long TTL) {
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TTL))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // * 토큰 생성 (TTL 인자 추가)
    public String generateToken(String useremail, long TTL) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, useremail, TTL);
    }

    // * 리프레시 토큰 생성 (만료 시간만 포함)
    public String generateRefreshToken(long TTL) {
        return createRefreshToken(TTL);
    }

    // 토큰 유효성 검사
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // * 메서드 위치 변경
    public Cookie createCookie(String key, String value, boolean isRefreshToken) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge((int) REFRESH_TOKEN_TTL);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "None");
        if (isRefreshToken) {
            cookie.setPath("/api/auth");
        } else {
            cookie.setPath("/");
        }
        cookie.setHttpOnly(true);
        return cookie;
    }

    public Cookie accessTokenRemover() {
        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setMaxAge(0);
        accessToken.setSecure(true);
        accessToken.setPath("/");
        accessToken.setHttpOnly(true);
        return accessToken;
    }

    public void storeRefreshToken(String username, String newRefreshToken){
        redisService.setData(username, newRefreshToken, Duration.ofMillis(REFRESH_TOKEN_TTL));
    }

}
