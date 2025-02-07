package com.toty.springconfig.sms;

import com.toty.common.baseException.NotificationSendException;
import com.toty.common.exception.ErrorCode;
import com.toty.common.exception.ExpectedException;
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
    private final SmsConfig smsConfig;
    private final DefaultMessageService messageService;
    private final UserRepository userRepository;

    @Async("notificationExecutor")
    public void sendSmsNotification(SmsNotificationSendRequest smsNotificationSendRequest) {
        User user = validatedSmsUser(smsNotificationSendRequest.getReceiverId());
        Message message = createMessage(user.getPhoneNumber(), smsNotificationSendRequest.getMessage());

        try {
            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            throw new NotificationSendException(e);
        }

    }

    // sms 알림 동의 확인 여부
    @NotNull
    private User validatedSmsUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        if (!user.getSubscribeInfo().isSmsSubscribed()) {
            throw new ExpectedException(ErrorCode.SMS_CONSENT_DENIED);
        }
        if (user.getPhoneNumber() == null) {
            throw new ExpectedException(ErrorCode.SMS_NOT_REGISTERED);
        }
        return user;
    }

    // 메세지 작성
    private Message createMessage(String to, String text) {
        Message message = new Message();
        message.setFrom(smsConfig.getMessageFrom());
        message.setTo(to);
        message.setText(text);
        return message;
    }
}
