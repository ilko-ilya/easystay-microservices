package com.samilyak.authservice.dto;

import com.samilyak.authservice.model.User;

public record UserRegistrationResponseDto(

        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role

) {
}
