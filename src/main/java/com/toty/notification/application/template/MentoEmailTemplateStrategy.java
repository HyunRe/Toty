package com.toty.notification.application.template;

import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class MentoEmailTemplateStrategy implements EmailTemplateStrategy {
    @Override
    public String getTemplate(Notification notification) {
        return "email/mento";
    }

    public void addAdditionalVariables(Context context, User user) {
        context.setVariable("nickname", user.getNickname());
    }
}

