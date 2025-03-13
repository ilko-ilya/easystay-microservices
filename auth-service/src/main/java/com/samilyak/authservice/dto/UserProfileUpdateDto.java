package com.samilyak.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateDto(

        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        String phoneNumber,
        String address

) {}
