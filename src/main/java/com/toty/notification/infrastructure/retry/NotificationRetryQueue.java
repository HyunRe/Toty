package com.toty.notification.infrastructure.retry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class NotificationRetryQueue {
    private final Queue<NotificationRetryEnvelope> queue = new ConcurrentLinkedQueue<>();

    public void offer(NotificationRetryEnvelope envelope) {
        queue.offer(envelope);
    }

    public NotificationRetryEnvelope poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<NotificationRetryEnvelope> drainAll() {
        List<NotificationRetryEnvelope> drained = new ArrayList<>();
        NotificationRetryEnvelope env;
        while ((env = queue.poll()) != null) {
            drained.add(env);
        }
        return drained;
    }
}
