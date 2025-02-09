package com.toty.springconfig.email.template;

import com.toty.springconfig.email.EmailNotificationSendRequest;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;


@Component
public class UnreadEmailTemplate implements EmailTemplate {
    @Override
    public String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest) {
        return "email/unread";
    }

    public void addAdditionalVariables(Context context, int unreadCount) {
        context.setVariable("unreadCount", unreadCount);
    }
}

