package com.samilyak.accommodationservice.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class FeignBasicAuthInterceptor implements RequestInterceptor {

    @Value("${service.security.username")
    private String username;

    @Value("${service.security.password}")
    private String password;

    @Override
    public void apply(RequestTemplate template) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        template.header("Authorization", "Basic " + encoded);
    }
}
