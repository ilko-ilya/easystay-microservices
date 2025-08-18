package com.samilyak.notification.service;

import com.samilyak.notification.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationService implements NotificationSender {

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public void send(NotificationDto dto) {
        String formattedMessage = formatMessage(dto);
        log.info("📱 [SMS ЗАГЛУШКА] Отправка: {}", formattedMessage);
        // TODO: Реальная интеграция с SMS API
    }

    private String formatMessage(NotificationDto dto) {
        return String.format(
                "Бронирование: ID %s, тел.%s. %s",
                dto.userId(), dto.phoneNumber(), dto.message()
        );
    }
}
