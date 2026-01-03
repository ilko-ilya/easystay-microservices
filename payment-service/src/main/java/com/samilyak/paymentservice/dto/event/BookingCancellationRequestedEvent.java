package com.samilyak.paymentservice.dto.event;

public record BookingCancellationRequestedEvent(

        Long bookingId,
        Long accommodationId,
        String paymentId,
        boolean refundNeeded

) {
}
