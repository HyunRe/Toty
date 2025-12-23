package com.toty.notification.domain.factory.url;

import com.toty.common.baseException.UnSupportedNotificationTypeException;
import com.toty.notification.domain.strategy.message.NotificationMessageStrategy;
import com.toty.notification.domain.strategy.url.NotificationUrlStrategy;
import com.toty.notification.domain.type.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationUrlFactory {
    private final Map<EventType, NotificationUrlStrategy> strategies;

    @Autowired
    public NotificationUrlFactory(List<NotificationUrlStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NotificationUrlStrategy::getEventType,
                        Function.identity()
                ));
    }

    public String generateUrl(EventType eventType, String referenceId) {
        NotificationUrlStrategy strategy = strategies.get(eventType);
        if (strategy != null) {
            return strategy.generateUrl(referenceId);
        }
        throw new UnSupportedNotificationTypeException(eventType);
    }
}
