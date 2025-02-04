package com.toty.notification.application.template;

import com.toty.notification.domain.model.Notification;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;


@Component
public class UnreadEmailTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public String getTemplate(Notification notification) {
        return "email/unread";
    }

    public void addAdditionalVariables(Context context, int unreadCount) {
        context.setVariable("unreadCount", unreadCount);
    }
}

