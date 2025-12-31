package com.toty.common.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 헬스 체크 모니터
 * - 주기적으로 애플리케이션 상태를 확인
 * - 장애 발생 시 AlertService에 알림 요청
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheckMonitor {
    private final HealthEndpoint healthEndpoint;
    private final AlertService alertService;

    private Status previousStatus = Status.UP;

    /**
     * 1분마다 헬스 체크 수행
     */
    @Scheduled(fixedRate = 60000) // 60초
    public void checkHealth() {
        try {
            HealthComponent healthComponent = healthEndpoint.health();
            Status currentStatus = healthComponent.getStatus();

            log.debug("Health check - status: {}", currentStatus);

            // 상태 변경 감지
            if (!currentStatus.equals(previousStatus)) {
                log.warn("시스템 상태 변경 감지: {} -> {}", previousStatus, currentStatus);

                if (Status.DOWN.equals(currentStatus) || Status.OUT_OF_SERVICE.equals(currentStatus)) {
                    // 장애 발생
                    alertService.sendCriticalAlert(
                            "시스템 장애 발생",
                            String.format("애플리케이션 상태: %s -> %s", previousStatus, currentStatus),
                            null
                    );
                } else if (Status.UP.equals(currentStatus) && !Status.UP.equals(previousStatus)) {
                    // 복구 감지
                    alertService.sendInfoAlert(
                            "시스템 복구 완료",
                            String.format("애플리케이션 상태: %s -> %s", previousStatus, currentStatus)
                    );
                }

                previousStatus = currentStatus;
            }
        } catch (Exception e) {
            log.error("헬스 체크 실패: {}", e.getMessage(), e);
            alertService.sendCriticalAlert(
                    "헬스 체크 실패",
                    "헬스 엔드포인트 조회 중 오류 발생: " + e.getMessage(),
                    null
            );
        }
    }
}
