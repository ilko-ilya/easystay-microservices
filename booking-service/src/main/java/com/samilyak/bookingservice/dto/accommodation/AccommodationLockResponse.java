package com.samilyak.bookingservice.dto.accommodation;

import java.math.BigDecimal;

public record AccommodationLockResponse(

        boolean success,
        String message,
        BigDecimal dailyRate

) {}