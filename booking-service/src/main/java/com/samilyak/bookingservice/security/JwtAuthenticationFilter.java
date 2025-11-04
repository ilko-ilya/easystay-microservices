package com.samilyak.bookingservice.security;

import com.samilyak.bookingservice.client.AuthClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 🔸 1. Если запрос межсервисный (Basic) → пропускаем
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            log.debug("🛡 Basic межсервисный запрос — JWT проверка не требуется");
            filterChain.doFilter(request, response);
            return;
        }

        // 🔸 2. Если нет Bearer токена → 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ Отсутствует Bearer токен");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Bearer token");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 💡 проверка токена через auth-service
            boolean valid = authClient.validateToken("Bearer " + token);
            if (!valid) {
                throw new RuntimeException("Invalid token");
            }

            String username = authClient.extractUsername("Bearer " + token);
            String role = authClient.extractUserRole("Bearer " + token);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, authorities)
            );

            log.info("✅ JWT valid: user='{}', role='{}'", username, role);

        } catch (Exception e) {
            log.error("❌ Ошибка проверки токена", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token validation failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
