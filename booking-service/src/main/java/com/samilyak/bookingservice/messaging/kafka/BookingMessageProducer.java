package com.samilyak.bookingservice.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samilyak.bookingservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.bookingservice.dto.event.BookingCreatedEvent;
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
public class BookingMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${application.kafka.topics.booking-cancellation-requested}")
    private String bookingCancellationTopic;

    @Value("${application.kafka.topics.booking-created}")
    private String bookingCreatedTopic;

    public void sendBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        log.info("Sending BookingCancellationRequestedEvent for bookingId={}", event.bookingId());

        Message<BookingCancellationRequestedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, bookingCancellationTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }

    public void sendBookingCreated(BookingCreatedEvent event) {
        try {
            String jsonPreview = objectMapper.writeValueAsString(event);
            log.info("🚀 [DEBUG] ОТПРАВЛЯЕМ В KAFKA: {}", jsonPreview);
        } catch (JsonProcessingException e) {
            log.error("⚠️ Не удалось сериализовать для лога", e);
        }

        log.info("Sending BookingCreatedEvent to topic: {}", bookingCreatedTopic);

        Message<BookingCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, bookingCreatedTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }
}
