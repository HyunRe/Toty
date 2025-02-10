package com.toty.springconfig.email;

import com.toty.notification.application.service.NotificationService;
import com.toty.roleRefreshScheduler.application.RoleRefreshScheduler;
import com.toty.user.application.UserService;
import com.toty.user.domain.model.User;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final UserService userService;
    private final RoleRefreshScheduler roleRefreshScheduler;
    private final NotificationService notificationService;

    // 멘토 이메일 알림 전송
    @PostMapping("/send-mento")
    public void sendMentoEmailNotification(@ModelAttribute @Valid EmailNotificationSendRequest emailNotificationSendRequest,
                                                             Model model) throws MessagingException {
        emailService.sendEmailNotification(emailNotificationSendRequest);
        roleRefreshScheduler.refreshRole();
        User receiverNickname = userService.findById(emailNotificationSendRequest.getReceiverId());
        model.addAttribute("receiverNickname", receiverNickname);
    }

    // 읽지 않은 알림 이메일 알림 전송
    @PostMapping("/send-unread")
    public void sendUnReadEmailNotification(@ModelAttribute @Valid EmailNotificationSendRequest emailNotificationSendRequest,
                                                              Model model) throws MessagingException {
        emailService.sendEmailNotification(emailNotificationSendRequest);
        int unReadCount = notificationService.countUnreadNotifications(emailNotificationSendRequest.getReceiverId());
        model.addAttribute("unReadCount", unReadCount);
    }
}
