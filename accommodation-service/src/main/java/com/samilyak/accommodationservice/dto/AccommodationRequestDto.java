package com.samilyak.accommodationservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationRequestDto(
        @NotBlank String type,
        @NotBlank String size,
        @NotNull AddressRequestDto location,
        @NotNull List<String> amenities,
        @NotNull @Min(0) BigDecimal dailyRate,
        @NotNull @Min(1) Integer availability
) {
}
