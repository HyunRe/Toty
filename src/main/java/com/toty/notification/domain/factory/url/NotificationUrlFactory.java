package com.toty.notification.domain.factory.url;

import com.toty.common.baseException.UnSupportedNotificationTypeException;
import com.toty.notification.domain.strategy.url.NotificationUrlStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationUrlFactory {
    private final Map<String, NotificationUrlStrategy> strategies;

    @Autowired
    public NotificationUrlFactory(List<NotificationUrlStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(s -> s.getClass().getSimpleName().replace("UrlStrategy", "").toUpperCase(), s -> s));
    }

    public String generateUrl(String type, String referenceId) {
        NotificationUrlStrategy strategy = strategies.get(type.toUpperCase());
        if (strategy != null) {
            return strategy.generateUrl(referenceId);
        }
        throw new UnSupportedNotificationTypeException(type);
    }
}
