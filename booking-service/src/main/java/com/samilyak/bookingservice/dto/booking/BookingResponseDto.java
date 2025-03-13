package com.samilyak.bookingservice.dto.booking;

import com.samilyak.bookingservice.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponseDto(

        Long id,
        Long userId,
        Long accommodationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        String phoneNumber,
        String paymentId,
        Booking.Status status

) {
}
