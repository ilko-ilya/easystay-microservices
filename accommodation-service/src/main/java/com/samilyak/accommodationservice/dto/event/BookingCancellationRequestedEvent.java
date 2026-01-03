package com.samilyak.accommodationservice.dto.event;

import java.time.LocalDate;

public record BookingCancellationRequestedEvent(

        Long bookingId,
        Long accommodationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String paymentId,
        boolean refundNeeded

) {
}
