package com.toty.notification.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.toty.common.baseException.UnknownEventTypeException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum EventType {
    COMMENT("Comment"),
    LIKE("Like"),
    FOLLOW("Follow"),
    QNA_POST("Qna"),
    MENTOR_POST("MentorPost"),
    MENTOR_CHAT("MentorChat"),
    BECOME_MENTOR("BecomeMentor"),
    REVOKE_MENTOR("RevokeMentor");

    private String event;

    EventType(String event) {
        this.event = event;
    }

    @JsonValue
    public String getEvent() {
        return event;
    }

    @JsonCreator
    public static EventType fromString(String event) {
        for (EventType eventType : EventType.values()) {
            if (eventType.event.equalsIgnoreCase(event)) {
                return eventType;
            }
        }
        throw new UnknownEventTypeException(event);
    }
}
