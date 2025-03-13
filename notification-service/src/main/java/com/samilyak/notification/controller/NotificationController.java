package com.samilyak.notification.controller;

import com.samilyak.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public String sendTestNotification(@RequestParam Long userId, @RequestParam String phoneNumber) {
        notificationService.sendBookingNotification(userId, phoneNumber, "Тестовое бронирование!");
        return "Уведомление отправлено!";
    }
}
