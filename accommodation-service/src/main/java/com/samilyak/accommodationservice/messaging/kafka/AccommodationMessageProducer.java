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
    private String datesUnlockedTopic;

    // УСПЕХ: Отправляем эстафету в Payment
    public void sendInventoryReserved(InventoryReservedEvent event) {
        log.info("📤 Sending Inventory Reserved: bookingId={}", event.bookingId());
        sendMessage(inventoryReservedTopic, String.valueOf(event.bookingId()), event);
    }

    // ПРОВАЛ: Сообщаем Booking Service об ошибке
    public void sendInventoryFailed(InventoryReservationFailedEvent event) {
        log.warn("📤 Sending Inventory Failed: bookingId={}, reason={}", event.bookingId(), event.reason());
        sendMessage(inventoryFailedTopic, String.valueOf(event.bookingId()), event);
    }

    // ОТЧЕТ: Даты разблокированы
    public void sendDatesUnlocked(DatesUnlockedEvent event) {
        log.info("📤 Sending Dates Unlocked: bookingId={}", event.bookingId());
        sendMessage(datesUnlockedTopic, String.valueOf(event.bookingId()), event);
    }

    private void sendMessage(String topic, String key, Object payload) {
        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .build();

        kafkaTemplate.send(message);
    }
}
