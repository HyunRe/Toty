package com.toty.springconfig.email.template;

import com.toty.springconfig.email.EmailNotificationSendRequest;

public interface EmailTemplate {
    String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest);
}

