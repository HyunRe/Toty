package com.toty.springconfig.email.template;

import com.toty.notification.domain.model.Notification;
import com.toty.springconfig.email.EmailNotificationSendRequest;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;


@Component
public class UnreadEmailTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest) {
        return "email/unread";
    }

    public void addAdditionalVariables(Context context, int unreadCount) {
        context.setVariable("unreadCount", unreadCount);
    }
}

