package com.toty.common.redis.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.toty.common.baseException.JsonProcessingCustomException;
import com.toty.notification.application.sender.NotificationSenderService;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.notification.infrastructure.retry.NotificationRetryEnvelope;
import com.toty.notification.infrastructure.retry.NotificationRetryQueue;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final NotificationSenderService notificationSenderService;
    private final NotificationRetryQueue notificationRetryQueue;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 재시도 큐에 넣을 때 사용할 초기 지연(ms)
    private static final long INITIAL_RETRY_DELAY_MS = 2000L;

    @Override
    public void onMessage(Message message, @NotNull byte[] pattern) {
        log.info("[REDIS-SUB] ========== Redis 메시지 수신 ==========");
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("[REDIS-SUB] 수신된 메시지: {}", body);

        NotificationSendRequest notificationSendRequest = convertFromJson(body);
        log.info("[REDIS-SUB] 파싱 완료: receiverId={}, eventType={}",
                notificationSendRequest.getReceiverId(), notificationSendRequest.getEventType());

        // Redis에서 온 요청임을 표시
        notificationSendRequest.withFromRedis(true);
        log.info("[REDIS-SUB] fromRedis 플래그 설정 완료");

        try {
            log.info("[REDIS-SUB] NotificationSenderService.send() 호출");
            notificationSenderService.send(notificationSendRequest);
            log.info("[REDIS-SUB] ✅ 알림 전송 성공");
        } catch (FirebaseMessagingException | MessagingException e) {
            log.error("[REDIS-SUB] ❌ 전송 실패, 재시도 큐에 등록 - {}", notificationSendRequest, e);
            enqueueForRetry(notificationSendRequest);
        } catch (Exception e) {
            log.error("[REDIS-SUB] ❌ 처리 중 알 수 없는 예외 발생 - {}", notificationSendRequest, e);
            enqueueForRetry(notificationSendRequest);
        }
        log.info("[REDIS-SUB] ========== Redis 메시지 처리 종료 ==========");
    }

    private void enqueueForRetry(NotificationSendRequest request) {
        // 래핑 후 큐에 넣기 (NotificationRetryEnvelope 타입이어야 함)
        NotificationRetryEnvelope envelope = new NotificationRetryEnvelope(request, INITIAL_RETRY_DELAY_MS);
        notificationRetryQueue.offer(envelope);
    }

    private NotificationSendRequest convertFromJson(String json) {
        try {
            return objectMapper.readValue(json, NotificationSendRequest.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingCustomException(e);
        }
    }
}