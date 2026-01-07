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
import org.springframework.http.ResponseCookie;
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

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure; // HTTPS 환경에서는 true

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String cookieSameSite; // SameSite 속성

    private final RedisService redisService;
    private final JwtBlacklistService jwtBlacklistService;

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

        cookie.setSecure(cookieSecure); // application.yaml 설정에 따라 Secure 속성 적용
        return cookie;
    }

    /**
     * HTTPS 환경에서 사용할 보안 쿠키 생성 (SameSite 속성 포함)
     * @param key 쿠키 이름
     * @param value 쿠키 값
     * @param isRefreshToken 리프레시 토큰 여부
     * @return ResponseCookie
     */
    public ResponseCookie createSecureResponseCookie(String key, String value, boolean isRefreshToken) {
        if (isRefreshToken) {
            return ResponseCookie.from(key, value)
                    .maxAge(REFRESH_TOKEN_TTL / 1000)
                    .path("/api/auth")
                    .httpOnly(true) // RefreshToken은 HttpOnly 유지 (보안)
                    .secure(cookieSecure) // HTTPS에서만 전송
                    .sameSite(cookieSameSite) // SameSite 속성
                    .build();
        } else {
            return ResponseCookie.from(key, value)
                    .maxAge(ACCESS_TOKEN_TTL / 1000)
                    .path("/")
                    .httpOnly(false) // AccessToken은 JavaScript에서 읽을 수 있도록 설정
                    .secure(cookieSecure) // HTTPS에서만 전송
                    .sameSite(cookieSameSite) // SameSite 속성
                    .build();
        }
    }

    public Cookie accessTokenRemover() {
        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setMaxAge(0);
        accessToken.setSecure(cookieSecure); // application.yaml 설정에 따라 Secure 속성 적용
        accessToken.setPath("/");
        accessToken.setHttpOnly(false); // JavaScript에서도 접근 가능하도록
        return accessToken;
    }

    /**
     * 액세스 토큰 삭제용 ResponseCookie 생성
     * @return ResponseCookie
     */
    public ResponseCookie accessTokenRemoverResponseCookie() {
        return ResponseCookie.from("accessToken", "")
                .maxAge(0)
                .path("/")
                .httpOnly(false)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
    }

    public void storeRefreshToken(String username, String newRefreshToken){
        redisService.setData(username, newRefreshToken, Duration.ofMillis(REFRESH_TOKEN_TTL));
    }

    /**
     * 리프레시 토큰 로테이션
     * - 기존 리프레시 토큰을 블랙리스트에 추가하고 새로운 리프레시 토큰 발급
     * @param oldRefreshToken 기존 리프레시 토큰
     * @param username 사용자 이름
     * @return 새로운 리프레시 토큰
     */
    public String rotateRefreshToken(String oldRefreshToken, String username) {
        // 기존 토큰을 블랙리스트에 추가
        Date expirationDate = extractExpiration(oldRefreshToken);
        jwtBlacklistService.addToBlacklist(oldRefreshToken, expirationDate);

        // 새로운 리프레시 토큰 생성 및 저장
        String newRefreshToken = generateRefreshToken(username);
        storeRefreshToken(username, newRefreshToken);

        return newRefreshToken;
    }

    /**
     * 토큰을 블랙리스트에 추가 (로그아웃 시 사용)
     * @param token JWT 토큰
     */
    public void blacklistToken(String token) {
        Date expirationDate = extractExpiration(token);
        jwtBlacklistService.addToBlacklist(token, expirationDate);
    }

    /**
     * 토큰 유효성 검사 (블랙리스트 체크 포함)
     * @param token JWT 토큰
     * @param username 사용자 이름
     * @return 유효하면 true, 아니면 false
     */
    public Boolean validateTokenWithBlacklist(String token, String username) {
        // 블랙리스트 체크
        if (jwtBlacklistService.isBlacklisted(token)) {
            return false;
        }

        // 기존 유효성 검사
        return validateToken(token, username);
    }

}
