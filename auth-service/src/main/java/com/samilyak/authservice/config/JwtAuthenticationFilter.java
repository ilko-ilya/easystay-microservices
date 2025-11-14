package com.samilyak.authservice.config;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("🔍 Проверяем запрос: {}", requestPath);

        // Пропускаем эндпоинты, которые не требуют проверки токена
        if (requestPath.startsWith("/api/auth/login")
                || requestPath.startsWith("/actuator")
                || requestPath.startsWith("/api/auth/register")
                || requestPath.startsWith("/api/auth/validate")
                || requestPath.startsWith("/api/auth/extract-username")
                || requestPath.startsWith("/api/auth/extract-role")) {
            log.debug("✅ Пропускаем без JWT-проверки: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем наличие токена
        String token = getToken(request);
        if (token == null) {
            log.warn("❌ Нет токена в запросе");
            filterChain.doFilter(request, response);
            return;
        }

        // Проверяем валидность токена
        if (jwtUtil.isValidToken(token)) {
            String userName = jwtUtil.getUserName(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ Аутентификация успешна: {}", userName);
        } else {
            log.warn("❌ Невалидный токен");
        }

        filterChain.doFilter(request, response);
    }


    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
