package com.toty.notification.presentation.contorller.view;

import com.toty.notification.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/notifications")
@RequiredArgsConstructor
public class NotificationViewController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public String getUnreadNotifications(@RequestParam Long userId) {
        notificationService.getUnreadNotifications(userId);
        return "main/header";
    }
}
