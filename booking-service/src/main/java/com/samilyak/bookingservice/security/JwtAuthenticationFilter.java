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
        log.info("🛠 Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ Токен не найден или имеет неверный формат!");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"error\": \"Forbidden\", \"message\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);

        boolean isValid;
        String username;
        String role;

        try {
            isValid = authClient.validateToken(token);
            username = authClient.extractUsername(token);
            role = authClient.extractUserRole(token);

            log.info("✅ Токен валиден: {}, Имя пользователя: {}, Роль: {}", isValid, username, role);
        } catch (Exception e) {
            log.error("❌ Ошибка валидации токена!", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token validation failed\"}");
            return;
        }

        if (!isValid || username == null || username.isEmpty() || role == null || role.isEmpty()) {
            log.error("❌ Токен не прошел валидацию или отсутствует имя пользователя/роль!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"error\": \"Unauthorized\", \"message\": \"Invalid token or missing username/role\"}");
            return;
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("🔐 Установлен Authentication для пользователя {} с ролью {}", username, role);

        filterChain.doFilter(request, response);
    }
}
