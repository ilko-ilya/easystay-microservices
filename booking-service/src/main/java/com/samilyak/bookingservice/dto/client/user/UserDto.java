package com.samilyak.bookingservice.dto.client.user;

public record UserDto(

        Long id,
        String firstName,
        String lastName,
        String email,
        String role

) {
}
