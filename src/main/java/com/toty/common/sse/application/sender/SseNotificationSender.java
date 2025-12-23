package com.toty.common.sse.application.sender;

import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.dto.SseNotificationSendRequest;
import com.toty.notification.application.sender.NotificationSender;
import com.toty.notification.application.sse.NotificationSseService;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {
    // 기존의 SseService 대신 NotificationSseService를 주입받습니다.
    private final NotificationSseService notificationSseService;

    @Override
    public void send(Notification notification) {
        log.info("[SSE-SENDER] SSE 전송 시작: id={}, receiverId={}, eventType={}",
                notification.getId(), notification.getReceiverId(), notification.getEventType());

        // SSE는 이미 생성된 Notification을 그대로 사용하므로 referenceId 불필요
        // URL과 message가 이미 Notification에 포함되어 있음
        SseNotificationSendRequest sseNotificationSendRequest = new SseNotificationSendRequest(
                notification.getId(),  // 알림 고유 ID (중복 방지용)
                notification.getReceiverId(),
                notification.getSenderId(),
                notification.getSenderNickname(),
                notification.getEventType(),
                "",  // referenceId는 빈 문자열 (이미 URL 생성 완료)
                notification.getMessage(),
                notification.getUrl()
        );

        log.info("[SSE-SENDER] SSE 요청 객체 생성 완료: url={}", notification.getUrl());

        // Emitter 타입 확인
        SseEmitterType emitterType = notificationSseService.getEmitterType();
        if (emitterType == null) {
            log.warn("[SSE-SENDER] EmitterType is null - defaulting to SINGLE");
            notificationSseService.sendNotification(sseNotificationSendRequest);
            return;
        }

        log.info("[SSE-SENDER] EmitterType: {}", emitterType);

        switch (emitterType) {
            case SINGLE:
                // 알림 발생 시 해당 사용자에게 토스트 알림을 전송
                log.info("[SSE-SENDER] SINGLE 모드로 전송");
                notificationSseService.sendNotification(sseNotificationSendRequest);
                break;

            case MULTI:
                // 다중 알림 발생 시 해당 사용자들에게 토스트 알림을 전송 (현재는 단일 요청)
                log.info("[SSE-SENDER] MULTI 모드로 전송");
                notificationSseService.sendMultipleNotifications(Collections.singletonList(sseNotificationSendRequest));
                break;

            default:
                log.warn("[SSE-SENDER] Unknown emitterType {} - fallback to single send", emitterType);
                notificationSseService.sendNotification(sseNotificationSendRequest);
        }

        log.info("[SSE-SENDER] ✅ SSE 전송 완료");
    }
}