package com.samilyak.accommodationservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    private static final Map<String, String> TRUSTED_SERVICES = Map.of(
            "booking-service", "booking123",
            "accommodation-service", "accommodation123",
            "payment-service", "payment123"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Basic ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String base64Credentials = header.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = decoded.split(":", 2);

            if (parts.length != 2) {
                log.warn("Invalid Basic Auth format");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Basic Auth");
                return;
            }

            String username = parts[0];
            String password = parts[1];

            if (!TRUSTED_SERVICES.containsKey(username) ||
                    !TRUSTED_SERVICES.get(username).equals(password)) {
                log.error("Unauthorized service: {}", username);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid service credentials");
                return;
            }

            log.info("Service {} authenticated successfully", username);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error decoding Basic Auth", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Basic Auth header");
        }
    }
}
