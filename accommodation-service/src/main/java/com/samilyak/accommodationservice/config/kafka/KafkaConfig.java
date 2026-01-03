package com.samilyak.accommodationservice.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${application.kafka.topics.dates-unlocked}")
    private String datesUnlockedTopic;

    @Bean
    public NewTopic datesUnlockedTopic() {
        return TopicBuilder.name(datesUnlockedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
