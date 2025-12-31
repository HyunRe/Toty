package com.toty.common.monitoring;

import java.util.Map;

/**
 * 알림 서비스 인터페이스
 * - 시스템 장애 및 중요 이벤트 알림
 */
public interface AlertService {
    /**
     * 긴급 알림 전송 (시스템 장애 등)
     * @param title 알림 제목
     * @param message 알림 내용
     * @param details 상세 정보 (선택)
     */
    void sendCriticalAlert(String title, String message, Map<String, Object> details);

    /**
     * 정보성 알림 전송 (시스템 복구 등)
     * @param title 알림 제목
     * @param message 알림 내용
     */
    void sendInfoAlert(String title, String message);

    /**
     * 경고 알림 전송
     * @param title 알림 제목
     * @param message 알림 내용
     */
    void sendWarningAlert(String title, String message);
}
