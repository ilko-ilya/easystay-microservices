package com.samilyak.accommodationservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationDto(

        Long id,
        String type,
        String size,
        AddressResponseDto location,
        List<String> amenities,
        BigDecimal dailyRate,
        Integer availability

) {
}
