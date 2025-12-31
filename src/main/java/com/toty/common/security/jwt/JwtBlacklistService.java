package com.toty.common.security.jwt;

import com.toty.common.redis.application.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * JWT 토큰 블랙리스트 서비스
 * - 로그아웃된 토큰을 Redis에 저장하여 재사용 방지
 * - 토큰 만료 시간만큼만 블랙리스트에 보관 (자동 삭제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistService {
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private final RedisService redisService;

    /**
     * 토큰을 블랙리스트에 추가
     * @param token JWT 토큰
     * @param expirationDate 토큰 만료 시간
     */
    public void addToBlacklist(String token, Date expirationDate) {
        long now = System.currentTimeMillis();
        long expirationTime = expirationDate.getTime();

        // 이미 만료된 토큰은 블랙리스트에 추가하지 않음
        if (expirationTime <= now) {
            log.debug("이미 만료된 토큰은 블랙리스트에 추가하지 않음");
            return;
        }

        // 만료까지 남은 시간 계산
        long ttl = expirationTime - now;
        String key = BLACKLIST_PREFIX + token;

        redisService.setData(key, "blacklisted", Duration.ofMillis(ttl));
        log.info("토큰 블랙리스트 추가 완료 - TTL: {}ms", ttl);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        String value = redisService.getData(key);
        return value != null;
    }

    /**
     * 블랙리스트에서 토큰 제거 (테스트용)
     * @param token JWT 토큰
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisService.deleteData(key);
        log.debug("토큰 블랙리스트 제거 완료");
    }
}
