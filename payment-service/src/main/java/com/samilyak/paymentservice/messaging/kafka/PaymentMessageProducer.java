package com.samilyak.paymentservice.messaging.kafka;

import com.samilyak.paymentservice.dto.event.PaymentCanceledEvent;
import com.samilyak.paymentservice.dto.event.PaymentFailedEvent;
import com.samilyak.paymentservice.dto.event.PaymentSuccessEvent;
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

    @Value("${application.kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${application.kafka.topics.payment-failed}")
    private String paymentFailedTopic;

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        log.info("📤 Sending PaymentSuccessEvent for bookingId={}", event.bookingId());
        sendMessage(paymentSuccessTopic, String.valueOf(event.bookingId()), event);
    }

    public void sendPaymentFailed(PaymentFailedEvent event) {
        log.warn("📤 Sending PaymentFailedEvent for bookingId={}", event.bookingId());
        sendMessage(paymentFailedTopic, String.valueOf(event.bookingId()), event);
    }

    public void sendPaymentCanceled(PaymentCanceledEvent event) {
        log.info("📤 Sending PaymentCanceledEvent for bookingId={}", event.bookingId());
        sendMessage(paymentCanceledTopic, String.valueOf(event.bookingId()), event);
    }

    private void sendMessage(String topic, String key, Object payload) {
        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .build();
        kafkaTemplate.send(message);
    }
}
