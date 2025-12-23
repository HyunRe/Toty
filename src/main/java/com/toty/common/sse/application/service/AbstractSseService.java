package com.toty.common.sse.application.service;

import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.infrastructure.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSseService {
    protected final SseEmitterRepository sseEmitterRepository;

    protected abstract String getDomain(); // "comment", "notification(comment & Following)", "like" 등
    protected abstract String getEventName(); // 실제 이벤트 이름 ("comment", "notification")
    protected abstract String getKey(Long id); // 저장소에서 Emitter를 찾을 때 사용할 키
    protected abstract SseEmitterType getSseEmitterType(); // SINGLE 또는 MULTI
    protected abstract Long getEmitterTimeout(); // Emitter 타임아웃 (ms)

    /**
     * SSE 연결을 구독하고 Emitter를 등록합니다.
     * @param id 게시글 ID, 사용자 ID 등 고유 식별자
     * @return 생성된 SseEmitter
     */
    public SseEmitter subscribe(Long id) {
        String key = getKey(id);
        SseEmitter emitter = new SseEmitter(getEmitterTimeout());

        // Emitter 타입에 따라 저장
        if (getSseEmitterType() == SseEmitterType.SINGLE) {
            sseEmitterRepository.saveSingle(key, emitter);
        } else {
            sseEmitterRepository.saveMulti(key, emitter);
        }

        // 제거 콜백 (한 번만 등록, 중복 제거 방지)
        Runnable removeCallback = getRunnable(key, emitter);

        emitter.onCompletion(removeCallback);
        emitter.onTimeout(removeCallback);
        emitter.onError((e) -> {
            log.warn("{} SSE 연결 오류 발생 - Key: {}, Error: {}", getEventName(), key, e.getMessage(), e);
            removeCallback.run(); // 오류 발생 시에도 정리
        });

        // 초기 연결 메시지 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected to " + getEventName() + " SSE"));
        } catch (IOException e) {
            log.error("SSE 연결 메시지 전송 실패 - Key: {}", key, e);
            removeCallback.run(); // 전송 실패 시 Emitter 정리
        }

        return emitter;
    }

    private Runnable getRunnable(String key, SseEmitter emitter) {
        boolean[] callbackExecuted = {false}; // 콜백 중복 실행 방지

        return () -> {
            if (callbackExecuted[0]) {
                log.debug("Callback already executed for key: {}", key);
                return; // 이미 실행됨
            }
            callbackExecuted[0] = true;

            log.info("Emitter disconnected or timed out. Removing. Key: {}, Type: {}", key, getSseEmitterType());
            if (getSseEmitterType() == SseEmitterType.SINGLE) {
                sseEmitterRepository.removeSingle(key);
            } else {
                sseEmitterRepository.removeMulti(key, emitter);
            }
        };
    }

    /**
     * 특정 ID와 관련된 Emitter에 이벤트를 전송합니다.
     * @param id 대상 ID (postId, userId 등)
     * @param data 전송할 데이터
     * @param <T> 데이터 타입
     */
    public <T> void sendEvent(Long id, T data) {
        String key = getKey(id);
        if (getSseEmitterType() == SseEmitterType.SINGLE) {
            sseEmitterRepository.sendToSingle(key, getEventName(), data, () -> sseEmitterRepository.removeSingle(key));
        } else { // MULTI
            // Multi Emitter의 경우, toRemove 콜백은 각 Emitter 자체를 제거하도록 합니다.
            sseEmitterRepository.sendToMulti(key, getEventName(), data, (emitterToRemove) -> sseEmitterRepository.removeMulti(key, emitterToRemove));
        }
    }

    // Dead Emitter 정리를 위한 스케줄러 메서드 (각 구현체에서 @Scheduled와 함께 사용)
    public void cleanupDeadEmitters() {
        if (getSseEmitterType() == SseEmitterType.SINGLE) {
            sseEmitterRepository.cleanupDeadSingle(getEventName());
        } else { // MULTI
            sseEmitterRepository.cleanupDeadMulti(getEventName());
        }
    }
}