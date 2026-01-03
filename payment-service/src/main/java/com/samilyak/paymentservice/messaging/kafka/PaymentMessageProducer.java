package com.samilyak.paymentservice.messaging.kafka;

import com.samilyak.paymentservice.dto.event.PaymentCanceledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topics.payment-canceled}")
    private String paymentCanceledTopic;

    public void sendPaymentCanceled(PaymentCanceledEvent event) {
        log.info("Sending PaymentCanceledEvent for bookingId={}", event.bookingId());

        Message<PaymentCanceledEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, paymentCanceledTopic)
                .setHeader(KafkaHeaders.KEY, String.valueOf(event.bookingId()))
                .build();

        kafkaTemplate.send(message);
    }

}
