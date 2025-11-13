package com.samilyak.authservice.service;

import com.samilyak.authservice.config.JwtUtil;
import com.samilyak.authservice.dto.UserLoginRequestDto;
import com.samilyak.authservice.dto.UserLoginResponseDto;
import com.samilyak.authservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.email(),
                        requestDto.password()
                )
        );

        User user = (User) authentication.getPrincipal();

        String userId = user.getId().toString();
        String email = user.getEmail();
        String role = user.getRole().name();

        String token = jwtUtil.generateToken(userId, email, role);

        return new UserLoginResponseDto(token);
    }
}
