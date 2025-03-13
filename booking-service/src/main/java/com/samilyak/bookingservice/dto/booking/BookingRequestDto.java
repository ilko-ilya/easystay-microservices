package com.samilyak.bookingservice.dto.booking;

import java.time.LocalDate;

public record BookingRequestDto(

        Long accommodationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String phoneNumber

) {
}
