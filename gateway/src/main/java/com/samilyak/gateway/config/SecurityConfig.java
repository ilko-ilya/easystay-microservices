package com.samilyak.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Slf4j
@EnableWebFluxSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRoleConverter jwtRoleConverter;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**", "/swagger-ui/**",
                                "/v3/api-docs/**", "/actuator/**").permitAll()

                        .pathMatchers(POST, "/api/accommodations/**").hasAuthority("MANAGER")
                        .pathMatchers(PUT, "/api/accommodations/**").hasAuthority("MANAGER")
                        .pathMatchers(DELETE, "/api/accommodations/**").hasAuthority("MANAGER")
                        .pathMatchers(GET, "/api/accommodations/**").hasAnyAuthority("CUSTOMER", "MANAGER")

                        // Bookings
                        .pathMatchers(POST, "/api/bookings/**").hasAnyAuthority("CUSTOMER", "MANAGER")
                        .pathMatchers(GET, "/api/bookings/**").hasAnyAuthority("CUSTOMER", "MANAGER")
                        .pathMatchers(PUT, "/api/bookings/**").hasAnyAuthority("CUSTOMER", "MANAGER")
                        .pathMatchers(DELETE, "/api/bookings/**").hasAnyAuthority("CUSTOMER", "MANAGER")

                        // Payments
                        .pathMatchers("/api/payments/**").hasAnyAuthority("CUSTOMER", "MANAGER")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleConverter))
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        log.info("🔐 Инициализация JWT Decoder с секретным ключом");

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");

        return NimbusReactiveJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
