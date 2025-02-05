package com.toty.notification.application.service;

import com.toty.base.exception.*;
import com.toty.notification.application.template.MentoEmailTemplateStrategy;
import com.toty.notification.application.template.EmailTemplateStrategy;
import com.toty.notification.application.template.UnreadEmailTemplateStrategy;
import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final List<EmailTemplateStrategy> strategies;  // 전략 리스트

    private static final String EMAIL_TITLE_PREFIX = "[TOTY] ";

    @Async
    public void sendEmailNotification(Notification notification) throws MessagingException {
        User user = userRepository.findById(notification.getReceiverId()).orElseThrow(UserNotFoundException::new);

        // 사용자 정보가 존재 하지 않거나 이메일 알림 동의를 하지 않았다 면 반환
        if (!user.getSubscribeInfo().isSmsSubscribed()) {
            throw new EmailSubscriptionException();
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

        // 메일 제목 설정
        messageHelper.setSubject(EMAIL_TITLE_PREFIX + notification.getMessage());

        // 메일 수신자 설정
        messageHelper.setTo(user.getEmail());

        // 동적 데이터 설정 (템플릿 변수에 전달)
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("content", notification.getMessage());
        String text = setContext(notification, user, emailValues);  // 전략에 맞는 템플릿 선택

        messageHelper.setText(text, true); // HTML 포맷으로 텍스트 설정

        // 이메일에 포함될 이미지 설정
        messageHelper.addInline("logo", new ClassPathResource("static/img/image-1.png"));
        messageHelper.addInline("notice-icon", new ClassPathResource("static/img/image-2.png"));

        // 메일 전송
        javaMailSender.send(message);
    }

    // 사용자 역할과 읽지 않은 알림 수에 따른 템플릿 선택
    private String setContext(Notification notification, User user, Map<String, String> emailValues) {
        EmailTemplateStrategy strategy = selectStrategy(notification);
        Context context = new Context();
        emailValues.forEach(context::setVariable);

        // 전략에 따른 추가 변수 설정
        if (strategy instanceof MentoEmailTemplateStrategy) {
            ((MentoEmailTemplateStrategy) strategy).addAdditionalVariables(context, user);
        } else if (strategy instanceof UnreadEmailTemplateStrategy) {
            ((UnreadEmailTemplateStrategy) strategy).addAdditionalVariables(context, notificationService.getUnreadNotificationCount(notification.getReceiverId()));
        }

        return templateEngine.process(strategy.getTemplate(notification), context);
    }

    private EmailTemplateStrategy selectStrategy(Notification notification) {
        Optional<User> user = userRepository.findById(notification.getReceiverId());

        // 멘토 역할 확인
        if (user.map(u -> u.getRole().name().equals("MENTOR")).orElse(false)) {
            return strategies.stream()
                    .filter(strategy -> strategy instanceof MentoEmailTemplateStrategy)
                    .findFirst()
                    .orElseThrow(MentoStrategyNotFoundException::new);
        }

        // 읽지 않은 알림 수 확인
        if (notificationService.getUnreadNotificationCount(notification.getReceiverId()) >= 10) {
            return strategies.stream()
                    .filter(strategy -> strategy instanceof UnreadEmailTemplateStrategy)
                    .findFirst()
                    .orElseThrow(UnreadStrategyNotFoundException::new);
        }

        // 기본 템플릿 처리
        return strategies.stream()
                .findFirst()
                .orElseThrow(DefaultStrategyNotFoundException::new);
    }
}
