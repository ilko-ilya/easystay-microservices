package com.samilyak.bookingservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_NOTIFICATION_QUEUE = "booking.notification.queue";
    public static final String BOOKING_NOTIFICATION_EXCHANGE = "booking.notification.exchange";
    public static final String BOOKING_NOTIFICATION_ROUTING_KEY = "booking.notification.routingKey";

    @Bean
    public Queue queue() {
        return new Queue(BOOKING_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(BOOKING_NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(BOOKING_NOTIFICATION_ROUTING_KEY);
    }
}
