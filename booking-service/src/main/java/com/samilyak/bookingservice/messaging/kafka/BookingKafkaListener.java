package com.samilyak.bookingservice.messaging.kafka;

import com.samilyak.bookingservice.dto.event.DatesUnlockedEvent;
import com.samilyak.bookingservice.dto.event.InventoryReservationFailedEvent;
import com.samilyak.bookingservice.dto.event.PaymentCanceledEvent;
import com.samilyak.bookingservice.dto.event.PaymentFailedEvent;
import com.samilyak.bookingservice.dto.event.PaymentSuccessEvent;
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

    // =========================================================================
    // 1. УСПЕШНАЯ ОПЛАТА (Payment Success)
    // =========================================================================
    @KafkaListener(topics = "${application.kafka.topics.payment-success}")
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("📨 Payment Success received: bookingId={}", event.bookingId());
        bookingSagaService.finalizeBookingCreation(event.bookingId(), event.paymentSessionId());
    }

    // =========================================================================
    // 2. ОШИБКА ИНВЕНТАРИЗАЦИИ (Inventory Failed)
    // =========================================================================
    @KafkaListener(topics = "${application.kafka.topics.inventory-failed}")
    public void onInventoryFailure(InventoryReservationFailedEvent event) {
        log.info("📨 Inventory Failed received: bookingId={}", event.bookingId());
        bookingSagaService.failBookingCreation(event.bookingId(), event.reason());
    }

    // =========================================================================
    // 3. ОШИБКА ОПЛАТЫ (Payment Failed)
    // =========================================================================
    @KafkaListener(topics = "${application.kafka.topics.payment-failed}")
    public void onPaymentFailure(PaymentFailedEvent event) {
        log.info("📨 Payment Failed received: bookingId={}", event.bookingId());
        bookingSagaService.failBookingCreation(event.bookingId(), event.reason());
    }

    // =========================================================================
    // 4. ОТМЕНА ОПЛАТЫ (Payment Canceled)
    // =========================================================================
    @KafkaListener(topics = "${application.kafka.topics.payment-canceled}")
    public void onPaymentCanceled(PaymentCanceledEvent event) {
        log.info("📨 Payment Canceled received: bookingId={}", event.bookingId());
        bookingSagaService.handlePaymentCanceled(event.bookingId());
    }

    // =========================================================================
    // 5. ДАТЫ РАЗБЛОКИРОВАНЫ (Dates Unlocked)
    // =========================================================================
    @KafkaListener(topics = "${application.kafka.topics.dates-unlocked}")
    public void onDatesUnlocked(DatesUnlockedEvent event) {
        log.info("📨 Dates Unlocked received: bookingId={}", event.bookingId());
        bookingSagaService.handleDatesUnlocked(event.bookingId());
    }
}
