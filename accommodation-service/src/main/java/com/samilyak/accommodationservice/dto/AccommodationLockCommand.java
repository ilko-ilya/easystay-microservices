package com.samilyak.accommodationservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AccommodationLockCommand(

        @NotNull(message = "Check-in date is required")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required")
        LocalDate checkOutDate,

        @NotNull(message = "Expected version is required")
        Long expectedVersion

) {
}