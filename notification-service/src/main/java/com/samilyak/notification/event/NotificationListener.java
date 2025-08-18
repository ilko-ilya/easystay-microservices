package com.samilyak.notification.event;

import com.samilyak.notification.config.RabbitMQConfig;
import com.samilyak.notification.dto.NotificationDto;
import com.samilyak.notification.service.NotificationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationFactory factory;

    @RabbitListener(queues = RabbitMQConfig.TELEGRAM_QUEUE)
    public void handleTelegram(NotificationDto dto) {
        processNotification(dto, "telegram");
    }

    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void handleSms(NotificationDto dto) {
        processNotification(dto, "sms");
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmail(NotificationDto dto) {
        processNotification(dto, "email");
    }

    private void processNotification(NotificationDto dto, String channel) {
        try {
            factory.getSender(channel).send(dto);
            log.info("✅ Уведомление отправлено через {}", channel);
        } catch (Exception e) {
            log.error("❌ Ошибка отправки через {}: {}", channel, e.getMessage());
        }
    }
}

