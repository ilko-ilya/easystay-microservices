package com.samilyak.bookingservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class FeignServiceAuthConfig {

    @Value("${service.security.username}")
    private String username;

    @Value("${service.security.password}")
    private String password;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return (RequestTemplate template) -> {
            // Получаем текущие заголовки
            var headers = template.headers();

            // Если есть Bearer → не трогаем (запрос от клиента)
            if (headers.containsKey("Authorization") &&
                    headers.get("Authorization").stream().anyMatch(h -> h.startsWith("Bearer "))) {
                return;
            }

            // Если нет Bearer → добавляем Basic (межсервисный запрос)
            String credentials = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
            template.header("Authorization", "Basic " + encoded);

            System.out.println("🔑 Using internal creds: " + username + ":" + password);

            System.out.println("🧩 Added Basic Auth for internal request → " + template.url());
        };
    }
}
