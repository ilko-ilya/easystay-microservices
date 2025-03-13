package com.samilyak.authservice.dto;

import com.samilyak.authservice.model.User;

public record UserDto(

        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role

) {}