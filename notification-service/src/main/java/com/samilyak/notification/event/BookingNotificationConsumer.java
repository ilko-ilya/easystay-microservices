package com.samilyak.notification.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingNotificationConsumer {

    @RabbitListener(queues = "booking.notification.queue")
    public void receiveNotification(String message) {
        log.info("Получено уведомление: {}", message);
    }
}
