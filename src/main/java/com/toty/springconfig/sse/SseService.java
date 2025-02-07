package com.toty.springconfig.sse;

import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SseService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    // 각 사용자에게 SSE 연결을 유지
    public SseEmitter createEmitter(Long userId) {
        requestCounts.putIfAbsent(userId, new AtomicInteger(0));
        if (requestCounts.get(userId).incrementAndGet() > 3) {
            throw new ExpectedException(ErrorCode.TOO_MANY_SSE_REQUESTS);
        }

        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    // 알림 발생 시 해당 사용자에게 토스트 알림을 전송
    public void sendNotification(SseNotificationSendRequest sseNotificationSendRequest) {
        SseEmitter emitter = emitters.get(sseNotificationSendRequest.getReceiverId());
        if (emitter != null) {
            try {
                emitter.send(sseNotificationSendRequest);
            } catch (IOException e) {
                // 각 사용자에게 SSE 연결을 해제
                emitters.remove(sseNotificationSendRequest.getReceiverId());
                requestCounts.get(sseNotificationSendRequest.getReceiverId()).decrementAndGet();
                throw new NotificationSendException(e);
            }
        }
    }

    // 다중 알림 발생 시 해당 사용자들에게 토스트 알림을 전송
    @Async("notificationExecutor")
    public void sendMultipleNotifications(List<SseNotificationSendRequest> sseNotificationSendRequests) {
        for (SseNotificationSendRequest sseNotificationSendRequest : sseNotificationSendRequests) {
            SseEmitter emitter = emitters.get(sseNotificationSendRequest.getReceiverId());
            if (emitter != null) {
                try {
                    emitter.send(sseNotificationSendRequest);
                } catch (IOException e) {
                    // 각 사용자에게 SSE 연결을 해제
                    emitters.remove(sseNotificationSendRequest.getReceiverId());
                    requestCounts.get(sseNotificationSendRequest.getReceiverId()).decrementAndGet();
                    throw new NotificationSendException(e);
                }
            }
        }
    }
}
