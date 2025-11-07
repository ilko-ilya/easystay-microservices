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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
    public GlobalFilter customFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            request.getHeaders().forEach((key, value) -> log.info("🛠 Gateway получил заголовок: {} = {}", key, value));

            if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                log.info("✅ Gateway передаёт заголовок Authorization: {}", authHeader);
            } else {
                log.warn("❌ В запросе нет заголовка Authorization!");
            }

            return chain.filter(exchange);
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

    @Bean
    public GlobalFilter traceIdFilter() {
        return (exchange, chain) -> {
            String traceId = UUID.randomUUID().toString();
            exchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Trace-Id", traceId)
                            .build())
                    .build();

            log.info("Gateway добавил traceId: {}", traceId);
            return chain.filter(exchange);
        };
    }
}

