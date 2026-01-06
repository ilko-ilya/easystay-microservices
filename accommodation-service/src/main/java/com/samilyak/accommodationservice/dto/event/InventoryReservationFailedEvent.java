package com.samilyak.accommodationservice.dto.event;

public record InventoryReservationFailedEvent(

        Long bookingId,
        Long userId,
        String reason

) {
}
