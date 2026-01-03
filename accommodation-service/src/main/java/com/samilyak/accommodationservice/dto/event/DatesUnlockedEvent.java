package com.samilyak.accommodationservice.dto.event;

public record DatesUnlockedEvent(

        Long bookingId,
        Long accommodationId

) {
}
