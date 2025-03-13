package com.samilyak.notification.event;

import com.samilyak.notification.config.RabbitMQConfig;
import com.samilyak.notification.dto.NotificationDto;
import com.samilyak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_NOTIFICATION_QUEUE)
    public void handleNotification(NotificationDto notificationDto) {
        log.info("Получено уведомление: {}", notificationDto);

        notificationService.sendNotification(notificationDto);
    }
}
