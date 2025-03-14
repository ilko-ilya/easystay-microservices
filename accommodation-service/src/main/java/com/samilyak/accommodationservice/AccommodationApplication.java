package com.samilyak.accommodationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
public class AccommodationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccommodationApplication.class, args);
    }
}