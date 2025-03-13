package com.samilyak.authservice.controller;

import com.samilyak.authservice.config.JwtUtil;
import com.samilyak.authservice.dto.UserLoginRequestDto;
import com.samilyak.authservice.dto.UserLoginResponseDto;
import com.samilyak.authservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto requestDto) {
        log.info("🔥 Логин запрос получен: {}", requestDto.email());
        return ResponseEntity.ok(authenticationService.authenticate(requestDto));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        log.info("🔍 Валидация токена в `auth-service`: {}", token);
        boolean isValid = jwtUtil.isValidToken(token.replace("Bearer ", ""));
        log.info("✅ Результат валидации токена: {}", isValid);
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/extract-username")
    public ResponseEntity<String> extractUsername(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        log.info("🔍 Извлечение имени пользователя из токена в `auth-service`: {}", token);
        String username = jwtUtil.getUserName(token.replace("Bearer ", ""));
        log.info("✅ Извлеченное имя пользователя: {}", username);
        return ResponseEntity.ok(username);
    }

    @GetMapping("/extract-role")
    public ResponseEntity<String> extractRole(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        log.info("🔍 Извлекаем роль пользователя из токена: {}", token);
        String role = jwtUtil.getUserRole(token.replace("Bearer ", ""));
        log.info("✅ Роль пользователя: {}", role);
        return ResponseEntity.ok(role);
    }

}
