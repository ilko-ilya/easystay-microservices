package com.samilyak.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GlobalFilterConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public GlobalFilter userInfoFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            log.info("📍 Gateway: {} {}", request.getMethod(), path);

            // Пропускаем публичные эндпоинты без изменений
            if (isPublicPath(path)) {
                log.info("✅ Публичный путь, пропускаем");
                return chain.filter(exchange);
            }

            // Для защищённых путей - добавляем информацию о пользователе

            String authHeader = request.getHeaders().getFirst(AUTHORIZATION);
            log.info("🧩 Authorization header получен: {}", authHeader);

            return exchange.getPrincipal()
                    .flatMap(principal -> {
                        log.info("🔑 Principal класс: {}", principal.getClass().getName());

                        if (principal instanceof JwtAuthenticationToken jwtToken) {
                            Jwt jwt = jwtToken.getToken();

                            String email = jwt.getSubject();
                            String userId = jwt.getClaimAsString("userId");
                            String role = jwt.getClaimAsString("role");

                            log.info("✅ Пользователь: userId={}, email={}, role={}", userId, email, role);

                            // Создаём новый request с данными пользователя
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId != null ? userId : "")
                                    .header("X-User-Role", role != null ? role : "")
                                    .header("X-User-Email", email != null ? email : "")
                                    .build();

                            log.info("✅ Передаём на сервис с заголовками пользователя");

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        }

                        log.warn("⚠️ Principal не JWT, пропускаем");
                        return chain.filter(exchange);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        if (!isPublicPath(request.getPath().value())) {
                            log.debug("ℹ️ Второй проход фильтра без Principal — пропускаем (внутренний вызов)");
                        }
                        return chain.filter(exchange);
                    }));
        };
    }

    //  Handle routing errors and return JSON 503 instead of HTML page
    @Bean
    public GlobalFilter errorHandlerFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .onErrorResume(ex -> {
                    log.error("Ошибка при маршрутизации через Gateway: {}", ex.getMessage());
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    String body = "{\"error\": \"Service temporarily unavailable\"}";
                    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer));
                });
    }

    //  Checks whether the path is public
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator");
    }
}
