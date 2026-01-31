package com.samilyak.paymentservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Value("${application.kafka.topics.payment-canceled}")
    private String paymentCanceledTopic;

    @Bean
    public NewTopic paymentCanceledTopic() {
        return TopicBuilder.name(paymentCanceledTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        return new DefaultErrorHandler(
                new FixedBackOff(1000L, 3)
        );
    }
}
