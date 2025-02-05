package com.toty.springconfig.email.template;

import com.toty.notification.domain.model.Notification;
import com.toty.springconfig.email.EmailNotificationSendRequest;

public interface EmailTemplateStrategy {
    String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest);
}

