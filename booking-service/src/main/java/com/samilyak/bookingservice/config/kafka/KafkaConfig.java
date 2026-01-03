package com.samilyak.bookingservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${application.kafka.topics.booking-cancellation-requested}")
    private String bookingCancellationTopic;

    @Bean
    public NewTopic bookingCancellationTopic() {
        return TopicBuilder.name(bookingCancellationTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

}
