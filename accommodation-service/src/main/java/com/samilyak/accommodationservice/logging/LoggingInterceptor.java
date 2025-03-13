package com.samilyak.accommodationservice.logging;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class LoggingInterceptor {

    @ModelAttribute
    public void logRequestHeaders(@RequestHeader Map<String, String> headers) {
        System.out.println("🔍 Headers in AccommodationService: " + headers);
    }
}