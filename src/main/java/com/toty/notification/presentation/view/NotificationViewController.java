package com.toty.notification.presentation.view;

import com.toty.notification.application.service.NotificationService;
import com.toty.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/notifications")
@RequiredArgsConstructor
public class NotificationViewController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public String getUnreadNotifications(@RequestParam Long userId, Model model) {
        List<Notification> unReadNotifications =  notificationService.getUnreadNotificationsSortedByDate(userId);
        model.addAttribute("unReadNotifications", unReadNotifications);
        return "main/header";
    }
}
