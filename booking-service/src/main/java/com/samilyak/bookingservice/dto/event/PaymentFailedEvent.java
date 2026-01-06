package com.samilyak.bookingservice.dto.event;

public record PaymentFailedEvent(

        Long bookingId,
        Long userId,
        String reason

) {
}
