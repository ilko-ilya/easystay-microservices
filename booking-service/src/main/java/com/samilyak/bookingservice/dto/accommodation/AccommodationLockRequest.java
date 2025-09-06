package com.samilyak.bookingservice.dto.accommodation;

import java.time.LocalDate;

public record AccommodationLockRequest(

        Long accommodationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long expectedVersion

) {}