package com.toty.common.security.jwt;

import com.toty.common.redis.application.RedisService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// * 리프레시 토큰 발행 메서드 추가, createCookie 메서드 jwtRequestFilter에서 여기로 옮겨옴
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_TTL; // 액세스 토큰 수명

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_TTL; // 리프레시 토큰 수명

    private final RedisService redisService;

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

    // * 액세스 토큰 생성 (기본 TTL 사용)
    public String generateAccessToken(String useremail) {
        return generateToken(useremail, ACCESS_TOKEN_TTL);
    }

    // * 리프레시 토큰 생성 (TTL 인자 추가)
    public String generateRefreshToken(long TTL) {
        return createRefreshToken(TTL);
    }

    // * 리프레시 토큰 생성 (기본 TTL 사용)
    public String generateRefreshToken(String useremail) {
        return generateToken(useremail, REFRESH_TOKEN_TTL);
    }

    // 토큰 유효성 검사
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // * 메서드 위치 변경
    public Cookie createCookie(String key, String value, boolean isRefreshToken) {
        Cookie cookie = new Cookie(key, value);

        // MaxAge는 초 단위이므로 밀리초를 초로 변환
        if (isRefreshToken) {
            cookie.setMaxAge((int) (REFRESH_TOKEN_TTL / 1000)); // 14일 (초 단위)
            cookie.setPath("/api/auth");
            cookie.setHttpOnly(true); // RefreshToken은 HttpOnly 유지 (보안)
        } else {
            cookie.setMaxAge((int) (ACCESS_TOKEN_TTL / 1000)); // 2시간 (초 단위)
            cookie.setPath("/");
            cookie.setHttpOnly(false); // AccessToken은 JavaScript에서 읽을 수 있도록 설정
        }

        cookie.setSecure(false); // 개발 환경에서는 false (HTTPS에서는 true로 변경 필요)
        return cookie;
    }

    public Cookie accessTokenRemover() {
        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setMaxAge(0);
        accessToken.setSecure(false);
        accessToken.setPath("/");
        accessToken.setHttpOnly(false); // JavaScript에서도 접근 가능하도록
        return accessToken;
    }

    public void storeRefreshToken(String username, String newRefreshToken){
        redisService.setData(username, newRefreshToken, Duration.ofMillis(REFRESH_TOKEN_TTL));
    }

}
