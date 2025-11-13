package com.samilyak.accommodationservice.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

    @Bean
    public RequestInterceptor tracingRequestInterceptor(Tracer tracer) {
        return requestTemplate -> {
            if (tracer.currentSpan() != null) {
                requestTemplate.header("X-B3-TraceId", tracer.currentSpan().context().traceId());
                requestTemplate.header("X-B3-SpanId", tracer.currentSpan().context().spanId());
                requestTemplate.header("X-B3-Sampled", "1");
            }
        };
    }
}
