package com.samilyak.accommodationservice.dto;

import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationUpdateDto(

        List<String> amenities,
        @Min(0) BigDecimal dailyRate,
        @Min(1) Integer availability

) {}