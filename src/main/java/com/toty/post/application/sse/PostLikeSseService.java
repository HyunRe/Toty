package com.toty.post.application.sse;

import com.toty.post.domain.event.PostLikeEvent;
import com.toty.common.sse.application.service.AbstractSseService;
import com.toty.common.sse.infrastructure.SseEmitterRepository;
import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.infrastructure.SseKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostLikeSseService extends AbstractSseService {
    private static final String DOMAIN = "like"; // "like" 도메인으로 명확히
    private static final String EVENT_NAME = "like";
    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30분

    public PostLikeSseService(SseEmitterRepository emitterRepository) {
        super(emitterRepository);
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
    protected String getKey(Long postId) {
        return SseKeyUtil.postLikeKey(DOMAIN, postId); // SseKeyUtil에 정의된 키 사용
    }

    @Override
    protected SseEmitterType getSseEmitterType() {
        return SseEmitterType.MULTI; // 게시글 하나에 여러 명의 좋아요 구독 가능
    }

    @Override
    protected Long getEmitterTimeout() {
        return EMITTER_TIMEOUT;
    }

    public void sendPostLike(Long postId, PostLikeEvent postLikeEvent) {
        sendEvent(postId, postLikeEvent);
    }

    // 주기적으로 죽은 좋아요 emitter 정리 (스케줄러)
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void cleanupPostLikeEmitters() {
        log.info("Running scheduled cleanup for PostLike SSE emitters.");
        cleanupDeadEmitters(); // AbstractSseService의 공통 정리 로직 호출
    }
}