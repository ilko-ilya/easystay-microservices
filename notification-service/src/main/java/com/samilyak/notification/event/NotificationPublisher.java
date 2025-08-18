package com.samilyak.notification.event;

import com.samilyak.notification.config.RabbitMQConfig;
import com.samilyak.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationDto dto, String channel) {
        String routingKey = switch (channel) {
            case "sms" -> RabbitMQConfig.SMS_ROUTING_KEY;
            case "email" -> RabbitMQConfig.EMAIL_ROUTING_KEY;
            case "telegram" -> RabbitMQConfig.TELEGRAM_ROUTING_KEY;
            default -> throw new IllegalArgumentException("Неизвестный канал: " + channel);
        };

        log.info("📨 Отправка уведомления [{}] в очередь: {}", channel, dto);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                dto
        );
    }
}