package com.samilyak.gateway;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouteLogger {

    private final RouteDefinitionLocator routeDefinitionLocator;

    @PostConstruct
    public void logRoutes() {
        log.info("✅ Gateway загружает маршруты...");
        routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .subscribe(routes -> routes.forEach(route ->
                        log.info("📌 ID: {}, URI: {}, Predicates: {}",
                                route.getId(), route.getUri(), route.getPredicates())));
    }
}
