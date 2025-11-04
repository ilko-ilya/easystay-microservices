package com.samilyak.accommodationservice.security;

import com.samilyak.accommodationservice.config.ServiceSecurityProperties;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAuthFilter extends OncePerRequestFilter {

    private final ServiceSecurityProperties securityProperties;

    @PostConstruct
    public void init() {
        log.info("✅ Trusted services loaded: {}", securityProperties.getTrusted());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        //  если это запрос от клиента с JWT токеном — пропускаем дальше
        if (header != null && header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //  если это внутренний вызов, но без Basic Auth — блокируем
        if (header == null || !header.startsWith("Basic ")) {
            log.warn("❌ Missing Basic Auth in internal request");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing Basic Auth");
            return;
        }

        log.info("🔍 Incoming Authorization header: {}", header);

        String decoded = new String(Base64.getDecoder().decode(header.substring(6)), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":", 2);

        if (parts.length != 2) {
            log.warn("❌ Malformed Basic header");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Malformed Basic header");
            return;
        }

        String username = parts[0];
        String password = parts[1];
        Map<String, String> trusted = securityProperties.getTrusted();

        log.info("👤 Parsed credentials: username='{}', password='{}'", username, password);
        log.info("🧱 Trusted map loaded: {}", trusted);
        log.info("🔎 Comparing '{}' with trusted keys {}", username, trusted.keySet());

        String match = trusted.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);

        if (match == null || !trusted.get(match).equals(password)) {
            log.warn("❌ Unauthorized internal call from '{}'", username);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid internal credentials");
            return;
        }

        log.info("✅ Internal service '{}' authenticated successfully", username);
        filterChain.doFilter(request, response);
    }
}
