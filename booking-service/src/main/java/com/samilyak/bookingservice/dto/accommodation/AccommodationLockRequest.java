package com.samilyak.bookingservice.dto.accommodation;

import java.time.LocalDate;

public record AccommodationLockRequest(

        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long expectedVersion

) {}