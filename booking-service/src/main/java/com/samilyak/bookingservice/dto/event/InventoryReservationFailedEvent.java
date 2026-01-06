package com.samilyak.bookingservice.dto.event;

public record InventoryReservationFailedEvent(

        Long bookingId,
        Long userId,
        String reason

) {
}
