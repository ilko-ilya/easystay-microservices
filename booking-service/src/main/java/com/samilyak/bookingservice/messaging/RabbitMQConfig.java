package com.samilyak.bookingservice.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "booking.notification.exchange";

    public static final String SMS_ROUTING_KEY = "booking.notification.sms";
    public static final String EMAIL_ROUTING_KEY = "booking.notification.email";
    public static final String TELEGRAM_ROUTING_KEY = "booking.notification.telegram";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }
}


