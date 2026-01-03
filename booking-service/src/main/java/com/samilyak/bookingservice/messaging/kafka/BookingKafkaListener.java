package com.samilyak.bookingservice.messaging.kafka;

import com.samilyak.bookingservice.dto.event.DatesUnlockedEvent;
import com.samilyak.bookingservice.dto.event.PaymentCanceledEvent;
import com.samilyak.bookingservice.saga.BookingSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingKafkaListener {

    private final BookingSagaService bookingSagaService;

    @KafkaListener(
            topics = "${application.kafka.topics.payment-canceled}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onPaymentCanceled(PaymentCanceledEvent event) {
        log.info("Received PaymentCanceledEvent for bookingId={}", event.bookingId());

        bookingSagaService.handlePaymentCanceled(event.bookingId());
    }

    @KafkaListener(
            topics = "${application.kafka.topics.dates-unlocked}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onDatesUnlocked(DatesUnlockedEvent event) {
        log.info("Received DatesUnlockedEvent for bookingId={}", event.bookingId());

        bookingSagaService.handleDatesUnlocked(event.bookingId());
    }
}
