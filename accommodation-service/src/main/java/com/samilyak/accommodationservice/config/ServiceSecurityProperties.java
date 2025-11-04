package com.samilyak.accommodationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "service.security")
public class ServiceSecurityProperties {

    private String username;
    private String password;
    private Map<String, String> trusted;

}