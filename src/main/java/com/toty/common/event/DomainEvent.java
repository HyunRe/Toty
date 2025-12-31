package com.toty.common.event;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트 인터페이스
 * - 모든 도메인 이벤트는 이 인터페이스를 구현해야 함
 */
public interface DomainEvent {
    /**
     * 이벤트 발생 시간
     */
    LocalDateTime occurredOn();

    /**
     * 이벤트 타입 (알림 타입과 매핑)
     */
    String getEventType();
}
