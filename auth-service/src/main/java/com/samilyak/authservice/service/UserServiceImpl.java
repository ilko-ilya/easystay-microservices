package com.samilyak.authservice.service;

import com.samilyak.authservice.dto.ChangePasswordDto;
import com.samilyak.authservice.dto.UserDto;
import com.samilyak.authservice.dto.UserProfileUpdateDto;
import com.samilyak.authservice.dto.UserRegistrationRequestDto;
import com.samilyak.authservice.dto.UserRegistrationResponseDto;
import com.samilyak.authservice.dto.UserRoleUpdateDto;
import com.samilyak.authservice.exception.InvalidPasswordException;
import com.samilyak.authservice.exception.RegistrationException;
import com.samilyak.authservice.exception.UserNotFoundException;
import com.samilyak.authservice.mapper.UserMapper;
import com.samilyak.authservice.model.User;
import com.samilyak.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        String email = requestDto.email().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RegistrationException("User with email " + email + " already exists.");
        }

        User user = userMapper.toModel(requestDto);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(requestDto.password()));

        if (user.getRole() == null) {
            user.setRole(User.Role.CUSTOMER);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }


    @Override
    public UserDto getCurrentUserProfile(Authentication authentication) {
        log.info("Authentication object: {}", authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        log.info("Retrieved user: {}", user);
        return userMapper.toDtoFromModel(user);
    }

    @Override
    public UserDto updateCurrentUserProfile(Authentication authentication, UserProfileUpdateDto updateDto) {
        User user = (User) authentication.getPrincipal();
        user.setFirstName(updateDto.firstName());
        user.setLastName(updateDto.lastName());
        user = userRepository.save(user);

        return userMapper.toDtoFromModel(user);
    }

    @Override
    public UserDto updateRoleById(Long userId, UserRoleUpdateDto roleUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setRole(roleUpdateDto.role());
        user = userRepository.save(user);

        return userMapper.toDtoFromModel(user);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("Can't fina User by this email: " + email));
    }


    @Override
    public void changePassword(Authentication authentication, ChangePasswordDto changePasswordDto) {
        Object principal = authentication.getPrincipal();
        log.info("Principal class: {}", principal.getClass().getName());

        User user = (User) principal;
        if (!passwordEncoder.matches(changePasswordDto.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", user.getEmail());
    }
}
