package com.toty.notification.infrastructure.email.application.service;

import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
import com.toty.common.baseException.NotificationSendException;
import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.type.EventType;
import com.toty.notification.infrastructure.email.dto.EmailNotificationSendRequest;
import com.toty.notification.infrastructure.email.domain.MentoEmailTemplate;
import com.toty.notification.infrastructure.email.domain.RevokeMentoEmailTemplate;
import com.toty.notification.infrastructure.email.domain.EmailTemplate;
import com.toty.notification.infrastructure.email.domain.UnreadEmailTemplate;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final List<EmailTemplate> strategies;  // 전략 리스트

    private static final String EMAIL_TITLE_PREFIX = "[TOTY] ";

    @Async("notificationExecutor")
    public void sendEmailNotification(EmailNotificationSendRequest emailNotificationSendRequest) throws MessagingException {
        User user = validatedEmailUser(emailNotificationSendRequest.getReceiverId());

        log.info("이메일 전송 시작: receiverId={}, email={}, eventType={}",
                emailNotificationSendRequest.getReceiverId(),
                user.getEmail(),
                emailNotificationSendRequest.getEventType());

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

        // 메일 제목 설정
        messageHelper.setSubject(EMAIL_TITLE_PREFIX + emailNotificationSendRequest.getMessage());

        // 메일 수신자 설정
        messageHelper.setTo(user.getEmail());

        // 동적 데이터 설정 (템플릿 변수에 전달)
        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("content", emailNotificationSendRequest.getMessage());
        String text = setContext(emailNotificationSendRequest, user, emailValues);  // 전략에 맞는 템플릿 선택

        messageHelper.setText(text, true); // HTML 포맷으로 텍스트 설정

        // 이메일에 포함될 이미지 설정
        messageHelper.addInline("logo", new ClassPathResource("static/img/image-1.png"));
        messageHelper.addInline("notice-icon", new ClassPathResource("static/img/image-2.png"));

        try {
            // 메일 전송
            javaMailSender.send(message);
            log.info("이메일 전송 성공: email={}", user.getEmail());
        } catch (Exception e) {
            log.error("이메일 전송 실패: email={}, error={}", user.getEmail(), e.getMessage(), e);
            throw new NotificationSendException(e);
        }
    }

    // email 알림 동의 확인 여부
    @NotNull
    private User validatedEmailUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        if (!user.getUserSubscribeInfo().isEmailSubscribed()) {
            throw new ExpectedException(ErrorCode.EMAIL_CONSENT_DENIED);
        }
        if (user.getEmail() == null) {
            throw new ExpectedException(ErrorCode.EMAIL_NOT_REGISTERED);
        }
        return user;
    }

    // 사용자 역할과 읽지 않은 알림 수에 따른 템플릿 선택
    private String setContext(EmailNotificationSendRequest emailNotificationSendRequest, User user, Map<String, String> emailValues) {
        EmailTemplate strategy = selectStrategy(emailNotificationSendRequest);
        Context context = new Context();
        emailValues.forEach(context::setVariable);

        // 전략에 따른 추가 변수 설정
        if (strategy instanceof MentoEmailTemplate) {
            ((MentoEmailTemplate) strategy).addAdditionalVariables(context, user);
        } else if (strategy instanceof RevokeMentoEmailTemplate) {
            ((RevokeMentoEmailTemplate) strategy).addAdditionalVariables(context, user);
        } else if (strategy instanceof UnreadEmailTemplate) {
            ((UnreadEmailTemplate) strategy).addAdditionalVariables(context, notificationService.countUnreadNotifications(emailNotificationSendRequest.getReceiverId()));
        }

        return templateEngine.process(strategy.getTemplate(emailNotificationSendRequest), context);
    }

    private EmailTemplate selectStrategy(EmailNotificationSendRequest emailNotificationSendRequest) {
        EventType eventType = emailNotificationSendRequest.getEventType();

        // 멘토 선정 이벤트
        if (eventType == EventType.BECOME_MENTOR) {
            return strategies.stream()
                    .filter(strategy -> strategy instanceof MentoEmailTemplate)
                    .findFirst()
                    .orElseThrow(() -> new ExpectedException(ErrorCode.MENTOR_TEMPLATE_NOT_FOUND));
        }

        // 멘토 해제 이벤트
        if (eventType == EventType.REVOKE_MENTOR) {
            return strategies.stream()
                    .filter(strategy -> strategy instanceof RevokeMentoEmailTemplate)
                    .findFirst()
                    .orElseThrow(() -> new ExpectedException(ErrorCode.REVOKE_MENTOR_TEMPLATE_NOT_FOUND));
        }

        // 읽지 않은 알림 수 확인 (10개 이상이면 안읽은 알림 템플릿 전송)
        if (notificationService.countUnreadNotifications(emailNotificationSendRequest.getReceiverId()) >= 10) {
            return strategies.stream()
                    .filter(strategy -> strategy instanceof UnreadEmailTemplate)
                    .findFirst()
                    .orElseThrow(() -> new ExpectedException(ErrorCode.UNREAD_NOTIFICATION_TEMPLATE_NOT_FOUND));
        }

        // 기본 템플릿 처리
        return strategies.stream()
                .findFirst()
                .orElseThrow(() -> new ExpectedException(ErrorCode.TEMPLATE_NOT_FOUND));
    }
}
