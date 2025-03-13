package com.samilyak.addressservice.dto;

public record AddressResponseDto(

        Long id,
        String country,
        String city,
        String street,
        String addressLine,
        Integer zipCode

) {}