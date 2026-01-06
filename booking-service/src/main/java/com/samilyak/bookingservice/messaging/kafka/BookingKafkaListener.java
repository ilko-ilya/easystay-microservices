package com.samilyak.bookingservice.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    // =========================================================================
    // 1. УСПЕШНАЯ ОПЛАТА (Payment Success)
    // =========================================================================
    @KafkaListener(
            topics = "${application.kafka.topics.payment-success}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onPaymentSuccess(String message) { // 👈 Принимаем String
        try {
            log.info("📨 RAW Payment Success: {}", message);
            PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);

            bookingSagaService.finalizeBookingCreation(event.bookingId(), event.paymentSessionId());
        } catch (Exception e) {
            log.error("❌ Error parsing PaymentSuccessEvent: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 2. ОШИБКА ИНВЕНТАРИЗАЦИИ (Inventory Failed)
    // =========================================================================
    @KafkaListener(
            topics = "${application.kafka.topics.inventory-failed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onInventoryFailure(String message) {
        try {
            log.info("📨 RAW Inventory Failed: {}", message);
            InventoryReservationFailedEvent event = objectMapper.readValue(message, InventoryReservationFailedEvent.class);

            bookingSagaService.failBookingCreation(event.bookingId(), event.reason());
        } catch (Exception e) {
            log.error("❌ Error parsing InventoryReservationFailedEvent: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 3. ОШИБКА ОПЛАТЫ (Payment Failed)
    // =========================================================================
    @KafkaListener(
            topics = "${application.kafka.topics.payment-failed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onPaymentFailure(String message) {
        try {
            log.info("📨 RAW Payment Failed: {}", message);
            // Тут используем класс события ошибки оплаты
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);

            bookingSagaService.failBookingCreation(event.bookingId(), event.reason());
        } catch (Exception e) {
            log.error("❌ Error parsing PaymentFailedEvent: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 4. ОТМЕНА ОПЛАТЫ (Payment Canceled)
    // =========================================================================
    @KafkaListener(
            topics = "${application.kafka.topics.payment-canceled}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onPaymentCanceled(String message) {
        try {
            log.info("📨 RAW Payment Canceled: {}", message);
            PaymentCanceledEvent event = objectMapper.readValue(message, PaymentCanceledEvent.class);

            bookingSagaService.handlePaymentCanceled(event.bookingId());
        } catch (Exception e) {
            log.error("❌ Error parsing PaymentCanceledEvent: {}", e.getMessage());
        }
    }

    // =========================================================================
    // 5. ДАТЫ РАЗБЛОКИРОВАНЫ (Dates Unlocked)
    // =========================================================================
    @KafkaListener(
            topics = "${application.kafka.topics.dates-unlocked}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onDatesUnlocked(String message) {
        try {
            log.info("📨 RAW Dates Unlocked: {}", message);
            DatesUnlockedEvent event = objectMapper.readValue(message, DatesUnlockedEvent.class);

            bookingSagaService.handleDatesUnlocked(event.bookingId());
        } catch (Exception e) {
            log.error("❌ Error parsing DatesUnlockedEvent: {}", e.getMessage());
        }
    }
}
