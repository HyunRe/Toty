package com.toty.notification.domain.repository;

import com.toty.notification.domain.model.FcmToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // 사용자 id로 FCM 토큰 조회
    Optional<FcmToken> findByUserId(Long userId);

    // 사용자 id로 FCM 토큰 목록 조회
    List<String> findTokensByUserId(Long userId);

    // 모든 FCM 토큰 조회
    @Query("SELECT f.token FROM FcmToken f")
    List<String> findAllTokens();
}
