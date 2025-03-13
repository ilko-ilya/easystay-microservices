package com.samilyak.paymentservice.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final HttpServletRequest request;

    @Override
    public void apply(RequestTemplate template) {
        String authHeader = request.getHeader(AUTHORIZATION);
        log.info("📡 Feign делает запрос -> метод: {}, URL: {}, Headers: {}",
                template.method(), template.url(), template.headers());

        if (authHeader != null && !authHeader.isEmpty()) {
            template.header(AUTHORIZATION, authHeader);
        }
    }
}
