package com.samilyak.accommodationservice.messaging.kafka;

import com.samilyak.accommodationservice.dto.event.DatesUnlockedEvent;
import com.samilyak.accommodationservice.dto.event.InventoryReservationFailedEvent;
import com.samilyak.accommodationservice.dto.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccommodationMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topics.inventory-reserved}")
    private String inventoryReservedTopic;

    @Value("${application.kafka.topics.inventory-failed}")
    private String inventoryFailedTopic;

    @Value("${application.kafka.topics.dates-unlocked}")
    private String topicName;

    //  УСПЕХ: Отправляем эстафету в Payment
    public void sendInventoryReserved(InventoryReservedEvent event) {
        log.info("📤 Sending InventoryReservedEvent for bookingId={}", event.bookingId());

        Message<InventoryReservedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, inventoryReservedTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }

    //  ПРОВАЛ: Сообщаем Booking Service об ошибке
    public void sendInventoryFailed(InventoryReservationFailedEvent event) {
        log.warn("📤 Sending InventoryReservationFailedEvent for bookingId={}", event.bookingId());

        Message<InventoryReservationFailedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, inventoryFailedTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }

    public void sendDatesUnlocked(DatesUnlockedEvent event) {
        log.info("📤 Sending dates unlocked event for booking {}", event.bookingId());

        Message<DatesUnlockedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }
}
