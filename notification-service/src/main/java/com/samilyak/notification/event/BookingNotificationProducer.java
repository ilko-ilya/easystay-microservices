package com.samilyak.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(String message) {
        rabbitTemplate.convertAndSend(
                "booking.notification.exchange",
                "booking.notification.routingKey",
                message
        );
    }
}
