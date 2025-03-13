package com.samilyak.authservice.dto;

public record UserLoginRequestDto(
        String email,
        String password
) {
}
