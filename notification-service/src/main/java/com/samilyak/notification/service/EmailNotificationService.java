package com.samilyak.notification.service;

import com.samilyak.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationSender {

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public void send(NotificationDto dto) {
        String formattedMessage = formatMessage(dto);
        log.info("📧 [EMAIL ЗАГЛУШКА] Отправка: {}", formattedMessage);
        // TODO: Реальная интеграция с Email API
    }

    private String formatMessage(NotificationDto dto) {
        return String.format(
                "Тема: Новое бронирование\n\n" +
                        "ID пользователя: %s\n" +
                        "Телефон: %s\n" +
                        "Сообщение: %s",
                dto.userId(), dto.phoneNumber(), dto.message()
        );
    }
}
