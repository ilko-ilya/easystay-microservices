package com.samilyak.notification.service;

import com.samilyak.notification.client.AuthClient;
import com.samilyak.notification.dto.NotificationDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TelegramNotificationService telegramNotificationService;
    private final AuthClient authClient;

    public void sendBookingNotification(Long userId, String phoneNumber, String message) {
        String username = "Неизвестный пользователь";

        try {
            username = authClient.getUsernameById(userId);
            log.info("✅ Получено имя пользователя: {}", username);
        } catch (FeignException.NotFound e) {
            log.warn("⚠ Пользователь с id {} не найден в auth-service", userId);
        } catch (FeignException e) {
            log.error("❌ Ошибка при обращении к auth-service: {}", e.getMessage());
        }

        // Формируем сообщение для Telegram
        String formattedMessage = String.format(
                "📢 Новое бронирование!\n👤 Пользователь: %s\n📞 Телефон: %s\n💬 %s",
                username, phoneNumber, message
        );

        log.info("📨 Отправка уведомления в Telegram: {}", formattedMessage);
        telegramNotificationService.sendNotification(formattedMessage);
    }

    public void sendNotification(NotificationDto notificationDto) {
        sendBookingNotification(
                notificationDto.userId(),
                notificationDto.phoneNumber(),
                notificationDto.message()
        );
    }
}
