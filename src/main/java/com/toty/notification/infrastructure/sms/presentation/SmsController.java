package com.toty.notification.infrastructure.sms.presentation;

import com.toty.notification.application.service.NotificationService;
import com.toty.notification.infrastructure.sms.dto.SmsNotificationSendRequest;
import com.toty.notification.infrastructure.sms.application.service.SmsService;
import com.toty.user.application.UserService;
import com.toty.user.domain.model.User;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {
    private final SmsService smsService;
    private final UserService userService;
    private final NotificationService notificationService;

    // 멘토 SMS 알림 전송
    @PostMapping("/send-mento")
    public void sendMentoSmsNotification(@ModelAttribute @Valid SmsNotificationSendRequest smsNotificationSendRequest,
                                             Model model) throws MessagingException {
        smsService.sendSmsNotification(smsNotificationSendRequest);
        User receiverNickname = userService.findById(smsNotificationSendRequest.getReceiverId());
        model.addAttribute("receiverNickname", receiverNickname);
    }

    // 읽지 않은 알림 SMS 알림 전송
    @PostMapping("/send-unread")
    public void sendUnReadSmsNotification(@ModelAttribute @Valid SmsNotificationSendRequest smsNotificationSendRequest,
                                              Model model) throws MessagingException {
        smsService.sendSmsNotification(smsNotificationSendRequest);
        int unReadCount = notificationService.countUnreadNotifications(smsNotificationSendRequest.getReceiverId());
        model.addAttribute("unReadCount", unReadCount);
    }
}
