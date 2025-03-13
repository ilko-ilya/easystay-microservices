package com.samilyak.authservice.controller;

import com.samilyak.authservice.dto.ChangePasswordDto;
import com.samilyak.authservice.dto.UserDto;
import com.samilyak.authservice.dto.UserProfileUpdateDto;
import com.samilyak.authservice.dto.UserRegistrationRequestDto;
import com.samilyak.authservice.dto.UserRegistrationResponseDto;
import com.samilyak.authservice.dto.UserRoleUpdateDto;
import com.samilyak.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> register(@RequestBody UserRegistrationRequestDto requestDto) {
        return ResponseEntity.status(CREATED).body(userService.register(requestDto));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(authentication));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileUpdateDto updateDto
    ) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(authentication, updateDto));
    }

    @GetMapping("/find-user-id")
    public ResponseEntity<Long> getUserIdByEmail(
            @RequestParam ("email") String email,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        log.info("📌 Запрос userId для email: {} с токеном: {}", email, token);

        return ResponseEntity.ok(userService.getUserIdByEmail(email));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordDto changePasswordDto)
    {
        userService.changePassword(authentication, changePasswordDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateRole(
            @PathVariable ("userId") Long userId,
            @RequestBody UserRoleUpdateDto roleUpdateDto
    ) {
        return ResponseEntity.ok(userService.updateRoleById(userId, roleUpdateDto));
    }
}
