package com.samilyak.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDto(

        @Email String email,
        @Size(min = 6) String password,
        @NotBlank String firstName,
        @NotBlank String lastName

) {
}
