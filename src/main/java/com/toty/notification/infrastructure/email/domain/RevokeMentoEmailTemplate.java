package com.toty.notification.infrastructure.email.domain;

import com.toty.notification.infrastructure.email.dto.EmailNotificationSendRequest;
import com.toty.user.domain.model.User;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class RevokeMentoEmailTemplate implements EmailTemplate {
    @Override
    public String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest) {
        return "email/revokeMento";
    }

    public void addAdditionalVariables(Context context, User user) {
        context.setVariable("nickname", user.getNickname());
    }
}
