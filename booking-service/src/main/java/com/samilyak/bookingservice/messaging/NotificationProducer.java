package com.samilyak.bookingservice.messaging;

import com.samilyak.bookingservice.config.rabbitmq.RabbitMQConfig;
import com.samilyak.bookingservice.dto.notification.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationDto dto, List<String> channels) {
        channels.forEach(channel -> {
            try {
                switch (channel.toLowerCase()) {
                    case "sms" -> sendSms(dto);
                    case "email" -> sendEmail(dto);
                    case "telegram" -> sendTelegram(dto);
                    default -> log.warn("⚠ Неизвестный канал: {}", channel);
                }
            } catch (Exception e) {
                log.error("❌ Ошибка отправки в {}: {}", channel, e.getMessage());
            }
        });
    }

    public void sendSms(NotificationDto dto) {
        log.info("📲 Отправка SMS уведомления: {}", dto);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.SMS_ROUTING_KEY,
                dto
        );
    }

    public void sendEmail(NotificationDto dto) {
        log.info("📧 Отправка Email уведомления: {}", dto);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                dto
        );
    }

    public void sendTelegram(NotificationDto dto) {
        log.info("🤖 Отправка Telegram уведомления: {}", dto);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.TELEGRAM_ROUTING_KEY,
                dto
        );
    }
}
