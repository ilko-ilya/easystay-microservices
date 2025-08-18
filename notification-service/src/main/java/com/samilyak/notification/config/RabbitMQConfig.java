package com.samilyak.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "booking.notification.exchange";

    public static final String SMS_QUEUE = "booking.notification.sms.queue";
    public static final String EMAIL_QUEUE = "booking.notification.email.queue";
    public static final String TELEGRAM_QUEUE = "booking.notification.telegram.queue";

    public static final String SMS_ROUTING_KEY = "booking.notification.sms";
    public static final String EMAIL_ROUTING_KEY = "booking.notification.email";
    public static final String TELEGRAM_ROUTING_KEY = "booking.notification.telegram";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(SMS_QUEUE, true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Queue telegramQueue() {
        return new Queue(TELEGRAM_QUEUE, true);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(smsQueue).to(exchange).with(SMS_ROUTING_KEY);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding telegramBinding(Queue telegramQueue, TopicExchange exchange) {
        return BindingBuilder.bind(telegramQueue).to(exchange).with(TELEGRAM_ROUTING_KEY);
    }
}
