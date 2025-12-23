package com.toty.notification.infrastructure.firebase.domain.repository;

import com.toty.notification.infrastructure.firebase.domain.model.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    // 특정 토큰에 해당하는 모든 엔티티 조회
    List<FcmToken> findByToken(String token);

    // 특정 사용자의 특정 토큰 존재 여부 (활성 여부 무관)
    Optional<FcmToken> findByUserIdAndToken(Long userId, String token);

    // 특정 사용자의 활성화된 토큰 문자열만 조회 (FCM 메시지 보낼 때 사용)
    @Query("SELECT f.token FROM FcmToken f WHERE f.user.id = :userId AND f.isActive = true")
    List<String> findActiveTokensByUserId(@Param("userId") Long userId);

    // 여러 사용자의 활성화된 토큰 문자열 조회 — "단체 알림" 필수
    @Query("SELECT f.token FROM FcmToken f WHERE f.user.id IN :userIds AND f.isActive = true")
    List<String> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    // 비활성 토큰 전체 삭제 — 무효 토큰 정리 시 사용
    void deleteByIsActiveFalse();
}
