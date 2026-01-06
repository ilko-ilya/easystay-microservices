package com.samilyak.paymentservice.dto.event;

public record PaymentFailedEvent(

        Long bookingId,
        Long userId,
        String reason

) {
}
