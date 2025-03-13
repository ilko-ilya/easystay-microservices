package com.samilyak.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;

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

}




