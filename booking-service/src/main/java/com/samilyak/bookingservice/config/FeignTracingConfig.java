package com.samilyak.bookingservice.config;

import feign.RequestInterceptor;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

    @Bean
    public RequestInterceptor tracingRequestInterceptor(Tracer tracer) {
        return template -> {
            if (tracer.currentSpan() != null) {
                template.header("X-B3-TraceId", tracer.currentSpan().context().traceId());
                template.header("X-B3-SpanId", tracer.currentSpan().context().spanId());
                template.header("X-B3-Sampled", "1");
            }
        };
    }
}
