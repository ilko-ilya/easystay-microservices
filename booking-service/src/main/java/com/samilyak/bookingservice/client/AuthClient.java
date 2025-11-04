package com.samilyak.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "auth-service", contextId = "authClient")
public interface AuthClient {

    @PostMapping("/api/auth/validate")
    boolean validateToken(@RequestHeader("Authorization") String token);

    @GetMapping("/api/auth/extract-username")
    String extractUsername(@RequestHeader(AUTHORIZATION) String token);

    @GetMapping("/api/auth/extract-role")
    String extractUserRole(@RequestHeader(AUTHORIZATION) String token);

}
