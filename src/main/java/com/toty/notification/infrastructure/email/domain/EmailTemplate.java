package com.toty.notification.infrastructure.email.domain;

import com.toty.notification.infrastructure.email.dto.EmailNotificationSendRequest;

public interface EmailTemplate {
    String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest);
}

