package com.samilyak.paymentservice.messaging.kafka;

import com.samilyak.paymentservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.paymentservice.dto.event.PaymentCanceledEvent;
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

    @KafkaListener(
            topics = "${application.kafka.topics.booking-cancellation-requested}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onBookingCancellationRequested(
            BookingCancellationRequestedEvent event
    ) {
        log.info(
                "Received BookingCancellationRequestedEvent bookingId={}, refundNeeded={}",
                event.bookingId(),
                event.refundNeeded()
        );

        if (!event.refundNeeded()) {
            log.info("No refund needed for bookingId={}", event.bookingId());
            return;
        }

        paymentService.cancelPayment(event.paymentId());

        paymentMessageProducer.sendPaymentCanceled(
                new PaymentCanceledEvent(
                        event.bookingId(),
                        event.paymentId()
                )
        );
    }
}
