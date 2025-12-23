package com.toty.notification.infrastructure.firebase.application.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.notification.infrastructure.firebase.dto.SendMulticastResult;
import com.toty.notification.infrastructure.firebase.infrastructure.MulticastResultConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {
    private static final int DEFAULT_BATCH_SIZE = 500;
    private final MulticastResultConfig multicast;
    private final FcmTokenService fcmTokenService;

    // 모든 알림 타입을 처리하는 공통 메소드 (단일 + 단체)
    public void sendFcmNotification(Collection<String> tokens, String title, String body, String type, String notificationId, String url, Map<String, String> extraData) {

        // 토큰 없음 → 전송 불가
        if (tokens == null || tokens.isEmpty()) {
            log.warn("[FCM SEND] No tokens provided. Skip send. (type={})", type);
            return;
        }

        // 공통 데이터 구성
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", type);
        data.put("notificationId", notificationId);

        if (url != null) data.put("url", url);
        if (extraData != null) data.putAll(extraData);

        try {
            SendMulticastResult result = multicast.sendMulticastResult(
                    tokens, title, body, data, DEFAULT_BATCH_SIZE, false
            );

            log.info("[FCM SEND] type={}, requested={}, success={}, failure={}",
                    type, result.getRequestedCount(), result.getSuccessCount(), result.getFailureCount());

            // 잘못된 토큰 제거
            if (!result.getInvalidTokens().isEmpty()) {
                log.warn("[FCM INVALID TOKENS] {}", result.getInvalidTokens());
                try {
                    fcmTokenService.deactivateInvalidTokens(result.getInvalidTokens());
                } catch (Exception e) {
                    log.error("[FCM INVALID TOKEN CLEANUP FAILED] {}", e.getMessage(), e);
                }
            }

        } catch (IllegalArgumentException e) {
            // 잘못된 인자 (토큰 형식 등)
            log.error("[FCM ERROR] IllegalArgumentException: invalid arguments. type={}, message={}",
                    type, e.getMessage(), e);

        } catch (Exception e) {
            // 기타 모든 예외를 캐치하여 서비스 전체 장애 방지
            log.error("[FCM ERROR] Unexpected exception while sending FCM. type={}, message={}",
                    type, e.getMessage(), e);
        }
    }
}


