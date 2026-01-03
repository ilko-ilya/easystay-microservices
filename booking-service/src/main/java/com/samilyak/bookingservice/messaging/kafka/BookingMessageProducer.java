package com.samilyak.bookingservice.messaging.kafka;

import com.samilyak.bookingservice.dto.event.BookingCancellationRequestedEvent;
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

    public void sendBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        log.info("Sending BookingCancellationRequestedEvent for bookingId={}", event.bookingId());

        Message<BookingCancellationRequestedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, bookingCancellationTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }
}
