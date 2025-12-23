package com.toty.notification.infrastructure.retry;

import com.toty.notification.dto.request.NotificationSendRequest;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class NotificationRetryEnvelope {
    private final NotificationSendRequest request;
    private final AtomicInteger attempts = new AtomicInteger(0);
    private volatile long nextAttemptAtMillis;

    public NotificationRetryEnvelope(NotificationSendRequest request, long initialDelayMillis) {
        this.request = request;
        this.nextAttemptAtMillis = System.currentTimeMillis() + initialDelayMillis;
    }

    public int incrementAndGetAttempts() {
        return attempts.incrementAndGet();
    }

    public int getAttempts() {
        return attempts.get();
    }

    public void scheduleNextAttempt(long delayMillis) {
        this.nextAttemptAtMillis = System.currentTimeMillis() + delayMillis;
    }

    @Override
    public String toString() {
        return "NotificationRetryEnvelope{" +
                "request=" + request +
                ", attempts=" + attempts +
                ", nextAttemptAtMillis=" + nextAttemptAtMillis +
                '}';
    }
}

