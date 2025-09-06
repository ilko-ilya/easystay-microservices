package com.samilyak.bookingservice.dto.accommodation;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationDto(

        Long id,
        String type,
        String size,
        List<String> amenities,
        BigDecimal dailyRate,
        Integer availability,
        Long version

) {
}
