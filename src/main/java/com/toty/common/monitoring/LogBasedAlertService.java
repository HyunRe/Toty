package com.toty.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 로그 기반 알림 서비스
 * - 현재는 로그만 남김
 * - 추후 Slack Webhook, Email, SMS 등으로 확장 가능
 */
@Slf4j
@Service
public class LogBasedAlertService implements AlertService {

    private static final String ALERT_PREFIX = "🚨 [ALERT]";
    private static final String INFO_PREFIX = "ℹ️ [INFO]";
    private static final String WARNING_PREFIX = "⚠️ [WARNING]";

    @Override
    public void sendCriticalAlert(String title, String message, Map<String, Object> details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        StringBuilder alertMessage = new StringBuilder();
        alertMessage.append(String.format("%s %s - %s%n", ALERT_PREFIX, title, timestamp));
        alertMessage.append(String.format("내용: %s%n", message));

        if (details != null && !details.isEmpty()) {
            alertMessage.append("상세 정보:%n");
            details.forEach((key, value) ->
                    alertMessage.append(String.format("  - %s: %s%n", key, value))
            );
        }

        log.error(alertMessage.toString());

        // TODO: 추후 Slack Webhook, Email, SMS 등으로 확장
        // slackWebhookClient.send(alertMessage.toString());
        // emailService.sendAlert(alertMessage.toString());
    }

    @Override
    public void sendInfoAlert(String title, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String alertMessage = String.format("%s %s - %s%n내용: %s",
                INFO_PREFIX, title, timestamp, message);

        log.info(alertMessage);

        // TODO: 추후 Slack Webhook 등으로 확장
    }

    @Override
    public void sendWarningAlert(String title, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String alertMessage = String.format("%s %s - %s%n내용: %s",
                WARNING_PREFIX, title, timestamp, message);

        log.warn(alertMessage);

        // TODO: 추후 Slack Webhook 등으로 확장
    }
}
