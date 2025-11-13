package com.samilyak.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication(scanBasePackages = "com.samilyak.gateway")
@EnableAutoConfiguration
public class GateWayApplication {
    public static void main(String[] args) {
        //  includes automatic transfer of reactive context between threads
        Hooks.enableAutomaticContextPropagation();

        SpringApplication.run(GateWayApplication.class, args);
    }
}