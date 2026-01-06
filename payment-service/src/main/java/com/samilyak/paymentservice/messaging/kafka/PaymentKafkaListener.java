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
    @KafkaListener(
            topics = "${application.kafka.topics.inventory-reserved}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("📨 Received InventoryReservedEvent for bookingId={}. Initializing payment...", event.bookingId());

        try {
            // Создаем сессию в Stripe и сохраняем платеж как PENDING
            paymentService.initiatePayment(event.bookingId(), event.userId(), event.totalPrice());

            log.info("✅ Payment initiated for bookingId={}. Waiting for user to pay.", event.bookingId());

            // ВАЖНО: Мы НЕ отправляем PaymentSuccess здесь.
            // Успех отправится только когда Stripe пришлет Webhook (или мы его сэмулируем).

        } catch (Exception e) {
            log.error("❌ Failed to initiate payment for bookingId={}: {}", event.bookingId(), e.getMessage());

            // Сообщаем Booking Service, что всё пропало
            paymentMessageProducer.sendPaymentFailed(new PaymentFailedEvent(
                    event.bookingId(),
                    event.userId(),
                    "Initialization failed: " + e.getMessage()
            ));
        }
    }

    @KafkaListener(
            topics = "${application.kafka.topics.booking-cancellation-requested}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        log.info("📨 Запрос на отмену платежа: bookingId={}, возврат={}", event.bookingId(), event.refundNeeded());

        // 1. ЛОГИКА ДЕНЕГ
        if (event.refundNeeded()) {
            try {
                // Ты сказал, что метод принимает String, поэтому приводим bookingId к строке
                paymentService.cancelPayment(String.valueOf(event.bookingId()));
                log.info("💰 Возврат оформлен через Stripe для брони {}", event.bookingId());
            } catch (Exception e) {
                log.error("❌ Ошибка при возврате денег: {}", e.getMessage());
            }
        } else {
            log.info("ℹ️ Возврат денег не требуется.");
        }

        paymentMessageProducer.sendPaymentCanceled(
                new PaymentCanceledEvent(
                        event.bookingId(),
                        event.paymentId()
                )
        );

        log.info("📤 Отправлено подтверждение отмены в Kafka для брони {}", event.bookingId());
    }
}
