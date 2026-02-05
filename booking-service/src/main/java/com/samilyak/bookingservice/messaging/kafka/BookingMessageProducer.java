package com.samilyak.bookingservice.messaging.kafka;

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

    @Value("${application.kafka.topics.booking-cancellation-requested}")
    private String bookingCancellationTopic;

    @Value("${application.kafka.topics.booking-created}")
    private String bookingCreatedTopic;

    public void sendBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        log.info("📤 Sending Cancellation Request: bookingId={}", event.bookingId());
        sendMessage(bookingCancellationTopic, String.valueOf(event.bookingId()), event);
    }

    public void sendBookingCreated(BookingCreatedEvent event) {
        log.info("📤 Sending Booking Created: bookingId={}", event.bookingId());
        sendMessage(bookingCreatedTopic, String.valueOf(event.bookingId()), event);
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
