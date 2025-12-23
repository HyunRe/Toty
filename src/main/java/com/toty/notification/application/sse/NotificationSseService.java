package com.toty.notification.application.sse;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.sse.application.service.AbstractSseService;
import com.toty.common.sse.infrastructure.SseEmitterRepository;
import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.infrastructure.SseKeyUtil;
import com.toty.common.sse.dto.SseNotificationSendRequest;
import com.toty.notification.dto.request.NotificationSendRequest;
import com.toty.notification.infrastructure.retry.NotificationRetryEnvelope;
import com.toty.notification.infrastructure.retry.NotificationRetryQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Slf4j
@Service
public class NotificationSseService extends AbstractSseService {
    private static final String DOMAIN = "notification";
    private static final String EVENT_NAME = "notification";
    private static final Long EMITTER_TIMEOUT = 5 * 60 * 1000L; // 5분
    private static final int MAX_SSE_REQUESTS = 3; // 최대 연결 시도 횟수
    private static final long INITIAL_RETRY_DELAY_MS = 2000L; // 재시도 초기 지연

    // 사용자별 연결 시도 횟수를 관리 (재연결 제한)
    private final Map<Long, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    private final NotificationRetryQueue retryQueue;
    private final SseEmitterRepository emitterRepository;

    public NotificationSseService(SseEmitterRepository emitterRepository,
                                  NotificationRetryQueue retryQueue) {
        super(emitterRepository);
        this.emitterRepository = emitterRepository;
        this.retryQueue = retryQueue;
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    protected String getEventName() {
        return EVENT_NAME;
    }

    @Override
    protected String getKey(Long userId) {
        return SseKeyUtil.notificationKey(DOMAIN, userId);
    }

    @Override
    protected SseEmitterType getSseEmitterType() {
        return SseEmitterType.SINGLE; // 사용자 한 명당 하나의 알림 Emitter 연결
    }

    public SseEmitterType getEmitterType() {
        return getSseEmitterType();
    }

    @Override
    protected Long getEmitterTimeout() {
        return EMITTER_TIMEOUT;
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        AtomicInteger count = requestCounts.compute(userId, (k, v) -> {
            if (v == null) return new AtomicInteger(1);
            v.incrementAndGet();
            return v;
        });

        if (count.get() > MAX_SSE_REQUESTS) {
            count.decrementAndGet();
            throw new ExpectedException(ErrorCode.TOO_MANY_SSE_REQUESTS);
        }

        // super.subscribe()가 이미 콜백 등록하므로 중복 제거
        SseEmitter emitter = super.subscribe(userId);

        // 기존 콜백에 더해서 requestCount 정리만 추가
        Runnable cleanupCount = () -> cleanupUserRequestCount(userId);
        emitter.onCompletion(cleanupCount);
        emitter.onTimeout(cleanupCount);
        emitter.onError(e -> cleanupCount.run());

        return emitter;
    }

    // 사용자 알림 전송 (단일)
    @Async("notificationExecutor")
    public void sendNotification(SseNotificationSendRequest request) {
        String key = getKey(request.getReceiverId());
        log.info("SSE 알림 전송 시도: receiverId={}, eventType={}, key={}",
            request.getReceiverId(), request.getEventType(), key);

        // Emitter 존재 여부 확인
        if (sseEmitterRepository.getSingle(key) == null) {
            log.warn("❌ SSE 연결 없음 - 재시도 큐에 추가: receiverId={}, eventType={}",
                    request.getReceiverId(), request.getEventType());
            NotificationRetryEnvelope envelope = new NotificationRetryEnvelope(request, INITIAL_RETRY_DELAY_MS);
            retryQueue.offer(envelope);
            return;
        }

        try {
            sendEvent(request.getReceiverId(), request); // AbstractSseService 제공
            log.info("✅ SSE 전송 성공: receiverId={}, eventType={}", request.getReceiverId(), request.getEventType());
        } catch (Exception e) {
            log.warn("SSE 전송 실패, 재시도 큐에 추가: receiverId={}, eventType={}, cause={}",
                    request.getReceiverId(), request.getEventType(), e.getMessage(), e);
            // SseNotificationSendRequest는 NotificationSendRequest를 상속받으므로 직접 사용 가능
            NotificationRetryEnvelope envelope = new NotificationRetryEnvelope(request, INITIAL_RETRY_DELAY_MS);
            retryQueue.offer(envelope);
        }
    }

    // 사용자 알림 전송 (다중, 비동기)
    @Async("notificationExecutor") // notificationExecutor 빈이 존재한다고 가정
    public void sendMultipleNotifications(List<SseNotificationSendRequest> requests) {
        requests.parallelStream().forEach(this::sendNotification);
    }

    // Emitter 정리 시 요청 카운트 감소
    private void cleanupUserRequestCount(Long userId) {
        AtomicInteger count = requestCounts.get(userId);
        if (count != null) {
            if (count.decrementAndGet() <= 0) {
                requestCounts.remove(userId); // 더 이상 연결 시도가 없으면 Map에서 제거
            }
        }
    }

    // 주기적으로 죽은 알림 emitter 정리 (스케줄러)
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void cleanupNotificationEmitters() {
        log.info("Running scheduled cleanup for Notification SSE emitters.");
        cleanupDeadEmitters(); // AbstractSseService의 공통 정리 로직 호출
    }
}