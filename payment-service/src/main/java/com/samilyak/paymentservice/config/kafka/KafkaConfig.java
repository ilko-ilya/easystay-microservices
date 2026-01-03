package com.samilyak.paymentservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${application.kafka.topics.booking-cancellation-requested}")
    private String bookingCancellationRequestedTopic;

    @Value("${application.kafka.topics.payment-canceled}")
    private String paymentCanceledTopic;

    /**
     * Топик, который payment-service СЛУШАЕТ
     * (создаём здесь, чтобы гарантировать partitions)
     */
    @Bean
    public NewTopic bookingCancellationRequestedTopic() {
        return TopicBuilder.name(bookingCancellationRequestedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Топик, который payment-service ПУБЛИКУЕТ
     */
    @Bean
    public NewTopic paymentCanceledTopic() {
        return TopicBuilder.name(paymentCanceledTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

}
