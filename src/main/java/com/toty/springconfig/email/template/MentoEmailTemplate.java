package com.toty.springconfig.email.template;

import com.toty.springconfig.email.EmailNotificationSendRequest;
import com.toty.user.domain.model.User;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
public class MentoEmailTemplate implements EmailTemplate {
    @Override
    public String getTemplate(EmailNotificationSendRequest emailNotificationSendRequest) {
        return "email/mento";
    }

    public void addAdditionalVariables(Context context, User user) {
        context.setVariable("nickname", user.getNickname());
    }
}

