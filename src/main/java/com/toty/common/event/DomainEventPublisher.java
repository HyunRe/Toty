package com.toty.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트 발행자
 * - Spring의 ApplicationEventPublisher를 래핑
 * - 도메인 계층에서 이벤트를 발행할 때 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 도메인 이벤트 발행
     * @param event 발행할 도메인 이벤트
     */
    public void publish(DomainEvent event) {
        log.info("도메인 이벤트 발행: {} - {}", event.getClass().getSimpleName(), event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }
}
