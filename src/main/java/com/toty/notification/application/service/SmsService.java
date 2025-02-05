package com.toty.notification.application.service;

import com.google.api.client.util.Value;
import com.toty.base.exception.PhoneNumberNotRegisteredException;
import com.toty.base.exception.NotificationSendException;
import com.toty.base.exception.SmsSubscriptionException;
import com.toty.base.exception.UserNotFoundException;
import com.toty.notification.domain.model.Notification;
import com.toty.user.domain.model.User;
import com.toty.user.domain.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {
    @Value("${spring.coolsms.api.fromnumber}")
    private String messageFrom;

    @Value("${spring.coolsms.api.key}")
    private String apiKey;

    @Value("${spring.coolsms.api.secret}")
    private String apiSecretKey;

    private final DefaultMessageService messageService;
    private final UserRepository userRepository;

    @Async
    public void sendSmsNotification(Notification notification) {
        User user = validatedSmsUser(notification.getReceiverId());
        Message message = createMessage(user.getPhoneNumber(), notification.getMessage());

        try {
            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }

    }

    @NotNull
    private User validatedSmsUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if (!user.getSubscribeInfo().isSmsSubscribed()) {
            throw new SmsSubscriptionException();
        }
        if (user.getPhoneNumber() == null) {
            throw new PhoneNumberNotRegisteredException();
        }
        return user;
    }

    private Message createMessage(String to, String text) {
        Message message = new Message();
        message.setFrom(messageFrom);
        message.setTo(to);
        message.setText(text);
        return message;
    }
}
