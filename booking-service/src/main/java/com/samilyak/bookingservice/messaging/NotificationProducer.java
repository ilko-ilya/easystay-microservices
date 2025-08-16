package com.samilyak.bookingservice.messaging;

import com.samilyak.bookingservice.dto.client.notification.NotificationRequestDto;
import com.samilyak.bookingservice.dto.notification.NotificationDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationDto notification) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_NOTIFICATION_EXCHANGE,
                RabbitMQConfig.BOOKING_NOTIFICATION_ROUTING_KEY,
                notification
        );
    }
}
