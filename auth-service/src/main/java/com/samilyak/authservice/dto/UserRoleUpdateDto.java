package com.samilyak.authservice.dto;

import com.samilyak.authservice.model.User;

public record UserRoleUpdateDto(

        User.Role role

) {}
