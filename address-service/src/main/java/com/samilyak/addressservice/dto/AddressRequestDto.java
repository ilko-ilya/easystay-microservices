package com.samilyak.addressservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDto(

        @NotBlank String country,
        @NotBlank String city,
        @NotBlank String street,
        @NotBlank String addressLine,
        @NotNull Integer zipCode

) {}