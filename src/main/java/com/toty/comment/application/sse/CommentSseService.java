package com.toty.comment.application.sse;

import com.toty.comment.domain.event.CommentEvent;
import com.toty.common.sse.application.service.AbstractSseService;
import com.toty.common.sse.infrastructure.SseEmitterRepository;
import com.toty.common.sse.domain.SseEmitterType;
import com.toty.common.sse.infrastructure.SseKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommentSseService extends AbstractSseService {
    private static final String DOMAIN = "comment";
    private static final String EVENT_NAME = "comment";
    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30분으로 변경 (클라이언트 재연결 고려)

    public CommentSseService(SseEmitterRepository emitterRepository) {
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
        return SseKeyUtil.commentKey(DOMAIN, postId);
    }

    @Override
    protected SseEmitterType getSseEmitterType() {
        return SseEmitterType.MULTI; // 게시글 하나에 여러 명의 댓글 구독 가능
    }

    @Override
    protected Long getEmitterTimeout() {
        return EMITTER_TIMEOUT;
    }

    /**
     * 특정 게시글에 댓글 이벤트를 전송합니다.
     * @param postId 대상 게시글 ID
     * @param commentEvent 전송할 댓글 이벤트 데이터
     */
    public void sendComment(Long postId, CommentEvent commentEvent) {
        try {
            sendEvent(postId, commentEvent);
        } catch (Exception e) {
            log.warn("Comment SSE 전송 실패: {}", commentEvent);
        }
    }

    // 주기적으로 죽은 댓글 emitter 정리 (스케줄러)
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void cleanupCommentEmitters() {
        log.info("Running scheduled cleanup for Comment SSE emitters.");
        cleanupDeadEmitters(); // AbstractSseService의 공통 정리 로직 호출
    }
}
