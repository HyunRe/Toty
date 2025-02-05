package com.toty.notification.application.service;

import com.toty.base.exception.NotificationSendException;
import com.toty.base.exception.RateLimitExceededException;
import com.toty.notification.domain.model.Notification;
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
            throw new RateLimitExceededException();
        }

        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    // 알림 발생 시 해당 사용자에게 알림을 전송
    public void sendNotification(Notification notification) {
        SseEmitter emitter = emitters.get(notification.getReceiverId());
        if (emitter != null) {
            try {
                emitter.send(notification);
            } catch (IOException e) {
                emitters.remove(notification.getReceiverId());
                throw new NotificationSendException(e);
            }
        }
    }

    // 다중 알림 발생 시 해당 사용자들에게 알림을 전송
    @Async
    public void sendMultipleNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            SseEmitter emitter = emitters.get(notification.getReceiverId());
            if (emitter != null) {
                try {
                    emitter.send(notification);
                } catch (IOException e) {
                    emitters.remove(notification.getReceiverId());
                    throw new NotificationSendException(e);
                }
            }
        }
    }

    // 각 사용자에게 SSE 연결을 해제
    private void removeEmitter(Long userId) {
        emitters.remove(userId);
        requestCounts.get(userId).decrementAndGet();
    }

    // 토스트 팝업 (프론트)
    public void sendToast(Notification notification) {
        // 토스트 팝업 전송 로직
        sendNotification(notification);
    }
}
