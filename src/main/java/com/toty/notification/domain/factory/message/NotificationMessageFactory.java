package com.toty.notification.domain.factory.message;

import com.toty.common.baseException.UnSupportedNotificationTypeException;
import com.toty.notification.domain.strategy.message.NotificationMessageStrategy;
import com.toty.notification.domain.type.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationMessageFactory {
    private final Map<EventType, NotificationMessageStrategy> strategies;

    @Autowired
    public NotificationMessageFactory(List<NotificationMessageStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NotificationMessageStrategy::getEventType,
                        Function.identity()
                ));
    }

    public String generateMessage(EventType eventType, String sender) {
        NotificationMessageStrategy strategy = strategies.get(eventType);
        if (strategy != null) {
            return strategy.generateMessage(sender);
        }
        throw new UnSupportedNotificationTypeException(eventType);
    }
}
