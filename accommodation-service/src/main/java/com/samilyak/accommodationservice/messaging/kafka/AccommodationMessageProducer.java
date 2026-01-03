package com.samilyak.accommodationservice.messaging.kafka;

import com.samilyak.accommodationservice.dto.event.DatesUnlockedEvent;
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

    @Value("${application.kafka.topics.dates-unlocked}")
    private String topicName;

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
