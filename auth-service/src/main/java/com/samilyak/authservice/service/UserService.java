package com.samilyak.authservice.service;

import com.samilyak.authservice.dto.ChangePasswordDto;
import com.samilyak.authservice.dto.UserDto;
import com.samilyak.authservice.dto.UserProfileUpdateDto;
import com.samilyak.authservice.dto.UserRegistrationRequestDto;
import com.samilyak.authservice.dto.UserRegistrationResponseDto;
import com.samilyak.authservice.dto.UserRoleUpdateDto;
import com.samilyak.authservice.model.User;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserService {

    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    UserDto getCurrentUserProfile(Authentication authentication);

    UserDto updateCurrentUserProfile(Authentication authentication, UserProfileUpdateDto updateDto);

    UserDto updateRoleById(Long userId, UserRoleUpdateDto roleUpdateDto);

    boolean existsById(Long userId);

    Long getUserIdByEmail(String email);

    void changePassword(Authentication authentication, ChangePasswordDto changePasswordDto);

}
