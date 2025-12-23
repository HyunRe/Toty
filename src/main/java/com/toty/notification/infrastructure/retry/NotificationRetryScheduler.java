package com.toty.notification.infrastructure.retry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toty.notification.application.sender.NotificationSenderService;
import com.toty.notification.domain.model.NotificationFailure;
import com.toty.notification.domain.repository.NotificationFailureRepository;
import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.exception.ExceptionUtils;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {
    private final NotificationRetryQueue retryQueue;
    private final NotificationSenderService notificationSenderService;

    private final NotificationFailureRepository failureRepository;
    private final MeterRegistry meterRegistry; // optional, micrometer for metrics
    private final ObjectMapper objectMapper; // jackson for payload serialization

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_MS = 2000L; // 2초

    @Scheduled(fixedDelay = 5000)
    public void retrySendFailedNotifications() {
        long now = System.currentTimeMillis();
        // 재시도 큐의 모든 항목을 순회(또는 drain한 후 검사)
        List<NotificationRetryEnvelope> drained = retryQueue.drainAll();
        for (NotificationRetryEnvelope envelope : drained) {
            try {
                // 예약된 시각이 아직 안되었으면 다시 큐에 넣기
                if (envelope.getNextAttemptAtMillis() > now) {
                    retryQueue.offer(envelope);
                    continue;
                }

                NotificationSendRequest request = envelope.getRequest();
                // 마킹: redis-origin 요청으로 처리
                request.withFromRedis(true);

                // 실제 전송 시도
                notificationSenderService.send(request);
                log.info("재전송 성공: {}", request);

            } catch (Exception e) {
                int attempts = envelope.incrementAndGetAttempts();
                if (attempts >= MAX_ATTEMPTS) {
                    // 영구 실패 처리: DB 저장, 알림 관리자 이메일, 모니터링 등
                    log.error("알림 영구 실패 (최대 재시도 초과): attempts={}, request={}", attempts, envelope.getRequest(), e);
                    handlePermanentFailure(envelope, e);
                } else {
                    long backoff = computeBackoffMillis(attempts);
                    envelope.scheduleNextAttempt(backoff);
                    log.warn("알림 재전송 실패, {}ms 후 재시도 예정 (attempt={}): request={}", backoff, attempts, envelope.getRequest(), e);
                    retryQueue.offer(envelope);
                }
            }
        }
    }

    private long computeBackoffMillis(int attempts) {
        // 지수 백오프 (최대 상한)
        long backoff = INITIAL_BACKOFF_MS * (1L << (attempts - 1));
        return Math.min(backoff, 60_000L); // 상한 60초
    }

    private void handlePermanentFailure(NotificationRetryEnvelope envelope, Exception e) {
        try {
            NotificationSendRequest request = envelope.getRequest();
            String payload = null;
            try {
                payload = objectMapper.writeValueAsString(request);
            } catch (Exception ex) {
                payload = "Failed to serialize payload: " + ex.getMessage();
            }

            String stack = Arrays.stream(ExceptionUtils.getStackTrace(e).split("\n"))
                    .limit(20) // 길이 제한
                    .collect(Collectors.joining("\n"));

            NotificationFailure failure = new NotificationFailure(
                    request.getReceiverId(),
                    request.getSenderId(),
                    request.getEventType().getEvent(),
                    request.getReferenceId(),
                    envelope.getAttempts(), // envelope에서 실제 시도 횟수 가져오기
                    e.getMessage(),
                    stack,
                    payload
            );
            failureRepository.save(failure);

            // 메트릭(선택)
            if (meterRegistry != null) {
                meterRegistry.counter("notifications.failure.permanent",
                        "type", request.getEventType().getEvent()).increment();
            }

            log.error("Notification permanent failure saved: receiver={}, event={}, attempts={}, failureId={}",
                    request.getReceiverId(), request.getEventType().getEvent(), envelope.getAttempts(), failure.getId(), e);
        } catch (Exception ex) {
            // 기록도 실패하면 로그라도 남긴다
            log.error("Failed to record permanent failure for notification: envelope={}, cause={}",
                    envelope, ex.getMessage(), ex);
        }
    }
}

