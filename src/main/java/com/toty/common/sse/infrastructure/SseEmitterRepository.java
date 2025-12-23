package com.toty.common.sse.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Slf4j
@Component
public class SseEmitterRepository {
    // 단일 연결용 emitter (사용자 알림 등)
    private final Map<String, SseEmitter> singleEmitters = new ConcurrentHashMap<>();

    // 다중 연결용 emitter (댓글/좋아요 등)
    private final Map<String, ConcurrentLinkedQueue<SseEmitter>> multiEmitters = new ConcurrentHashMap<>();
    // ================= SINGLE =================
    public void saveSingle(String key, SseEmitter emitter) {
        SseEmitter existing = singleEmitters.put(key, emitter);
        if (existing != null) {
            try {
                existing.complete();
            } catch (Exception e) {
                log.warn("기존 SINGLE emitter 종료 실패: {}", e.getMessage());
            }
        }
        log.info("Saved SINGLE emitter: {}", key);
    }

    public SseEmitter getSingle(String key) {
        return singleEmitters.get(key);
    }

    public void removeSingle(String key) {
        singleEmitters.remove(key);
        log.info("Removed SINGLE emitter: {}", key);
    }

    // ================= MULTI =================
    public void saveMulti(String key, SseEmitter emitter) {
        multiEmitters.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(emitter);
        log.info("Saved MULTI emitter: {}", key);
    }

    public Collection<SseEmitter> getMulti(String key) {
        return multiEmitters.getOrDefault(key, new ConcurrentLinkedQueue<>());
    }

    public void removeMulti(String key, SseEmitter emitter) {
        ConcurrentLinkedQueue<SseEmitter> emitters = multiEmitters.get(key);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                multiEmitters.remove(key);
            }
        }
        log.info("Removed MULTI emitter: {} for key: {}", emitter, key);
    }

    // ================= CLEANUP =================
    // DEAD emitter 정리 (멀티) - send 대신 상태 확인만 수행
    public void cleanupDeadMulti(String eventName) {
        multiEmitters.forEach((key, queue) -> {
            // 빈 큐는 맵에서 제거
            if (queue.isEmpty()) {
                multiEmitters.remove(key);
                log.debug("Removed empty MULTI queue for key: {}", key);
            }
        });
        log.debug("MULTI cleanup completed for {}", eventName);
    }

    // DEAD emitter 정리 (싱글) - send 대신 상태 확인만 수행
    public void cleanupDeadSingle(String eventName) {
        // 실제 정리는 onCompletion/onTimeout/onError 콜백에서 처리됨
        log.debug("SINGLE cleanup completed for {}", eventName);
    }

    // ================= SEND =================
    /**
     * - Broken pipe 에러 방지를 위한 개선
     * - ConcurrentModificationException 방지를 위해 리스트로 복사
     * - 전송 실패 시 안전하게 정리
     */
    public <T> void sendToMulti(String key, String eventName, T data, Consumer<SseEmitter> removeCallback) {
        ConcurrentLinkedQueue<SseEmitter> emitters = multiEmitters.get(key);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No MULTI emitters found for key: {}", key);
            return;
        }

        // ConcurrentModificationException 방지를 위해 복사본 생성
        List<SseEmitter> emitterList = new ArrayList<>(emitters);

        for (SseEmitter emitter : emitterList) {
            // 이미 큐에서 제거된 emitter는 건너뛰기
            if (!emitters.contains(emitter)) {
                log.debug("Emitter already removed, skipping: {}", emitter);
                continue;
            }

            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                log.debug("Successfully sent event '{}' to emitter for key: {}", eventName, key);
            } catch (Exception e) {
                log.warn("MULTI send failed for key {}: {}. Removing emitter.", key, e.getMessage());

                // 안전하게 complete 시도 (이미 끊긴 연결이면 에러 무시)
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.debug("Error while completing failed emitter: {}", ex.getMessage());
                }

                // 콜백 호출하여 repository에서 제거
                removeCallback.accept(emitter);
            }
        }
    }

    /**
     * 단일 emitter도 동일한 에러 처리 적용
     */
    public <T> void sendToSingle(String key, String eventName, T data, Runnable removeCallback) {
        SseEmitter emitter = getSingle(key);
        if (emitter == null) {
            log.warn("❌ No SINGLE emitter found for key: {} - 사용자가 SSE에 연결되지 않았습니다!", key);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
            log.info("✅ Successfully sent event '{}' to single emitter for key: {}", eventName, key);
        } catch (Exception e) {
            log.warn("SINGLE send failed for key {}: {}. Removing emitter.", key, e.getMessage());

            // 안전하게 complete 시도
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.debug("Error while completing failed emitter: {}", ex.getMessage());
            }

            // 콜백 호출하여 repository에서 제거
            removeCallback.run();
        }
    }
}

