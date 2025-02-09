package com.toty.springconfig.fcm.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // 사용자 id로 FCM 토큰 조회
    Optional<FcmToken> findByUserId(Long userId);

    // 사용자 id로 FCM 토큰 목록 조회
    List<String> findTokensByUserId(Long userId);
}
