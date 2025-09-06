package com.samilyak.accommodationservice.dto;

import java.math.BigDecimal;

public record AccommodationLockResult(

        boolean success,
        String message,
        BigDecimal dailyRate

) {}