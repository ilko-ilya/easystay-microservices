package com.samilyak.paymentservice.dto.event;

public record PaymentCanceledEvent(

        Long bookingId,
        String paymentId

) {
}
