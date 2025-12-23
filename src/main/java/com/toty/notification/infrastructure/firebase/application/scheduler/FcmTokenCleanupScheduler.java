package com.toty.notification.infrastructure.firebase.application.scheduler;

import com.toty.notification.infrastructure.firebase.application.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 비활성화된 FCM 토큰을 주기적으로 삭제하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTokenCleanupScheduler {
    private final FcmTokenService fcmTokenService;

    /**
     * 매일 새벽 3시에 비활성화된 FCM 토큰 정리
     * - 만료된 토큰, 로그아웃한 기기의 토큰 등 제거
     * - DB 용량 최적화
     */
    @Scheduled(cron = "0 0 3 * * ?")  // 매일 03:00:00
    public void cleanupInactiveTokens() {
        log.info("[FCM TOKEN CLEANUP] Starting scheduled cleanup of inactive tokens");

        try {
            int deletedCount = fcmTokenService.deleteInactiveTokens();

            if (deletedCount > 0) {
                log.info("[FCM TOKEN CLEANUP] Successfully deleted {} inactive tokens", deletedCount);
            } else {
                log.info("[FCM TOKEN CLEANUP] No inactive tokens to delete");
            }
        } catch (Exception e) {
            log.error("[FCM TOKEN CLEANUP] Failed to cleanup inactive tokens", e);
        }
    }
}
