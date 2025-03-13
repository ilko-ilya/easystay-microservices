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

    public void sendNotification(Long userId, String phone, String message) {

        NotificationDto notificationDto = new NotificationDto(userId, phone, message);

        log.info("Отправка уведомления в очередь: {}", notificationDto);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_NOTIFICATION_EXCHANGE,
                RabbitMQConfig.BOOKING_NOTIFICATION_ROUTING_KEY,
                notificationDto
        );
    }
}