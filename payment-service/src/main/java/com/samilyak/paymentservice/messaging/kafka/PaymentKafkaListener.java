package com.samilyak.paymentservice.messaging.kafka;

import com.samilyak.paymentservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.paymentservice.dto.event.InventoryReservedEvent;
import com.samilyak.paymentservice.dto.event.PaymentCanceledEvent;
import com.samilyak.paymentservice.dto.event.PaymentFailedEvent;
import com.samilyak.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaListener {

    private final PaymentService paymentService;
    private final PaymentMessageProducer paymentMessageProducer;

    // 1. ИНИЦИАЛИЗАЦИЯ ПЛАТЕЖА (Пришло от Accommodation)
    @KafkaListener(topics = "${application.kafka.topics.inventory-reserved}")
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("📨 Received InventoryReservedEvent: bookingId={}", event.bookingId());

        // Теперь, если база упадет, Spring будет ретраить (3 раза по 1 сек).
        paymentService.initiatePayment(event.bookingId(), event.userId(), event.totalPrice());

        log.info("✅ Payment initiated request processing for bookingId={}", event.bookingId());
    }

    // 2. ОТМЕНА ПЛАТЕЖА
    @KafkaListener(topics = "${application.kafka.topics.booking-cancellation-requested}")
    public void onBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        log.info("📨 Cancellation Request: bookingId={}, refund={}", event.bookingId(), event.refundNeeded());

        if (event.refundNeeded()) {
            paymentService.cancelPayment(String.valueOf(event.bookingId()));
            log.info("💰 Refund processed via Stripe for booking {}", event.bookingId());
        }

        paymentMessageProducer.sendPaymentCanceled(
                new PaymentCanceledEvent(event.bookingId(), event.paymentId())
        );
    }
}
