package com.toty.notification.application.template;

import com.toty.notification.domain.model.Notification;

public interface EmailTemplateStrategy {
    String getTemplate(Notification notification);
}

