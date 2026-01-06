package com.samilyak.paymentservice.controller;

import com.samilyak.paymentservice.dto.event.PaymentFailedEvent;
import com.samilyak.paymentservice.dto.event.PaymentSuccessEvent;
import com.samilyak.paymentservice.messaging.kafka.PaymentMessageProducer;
import com.samilyak.paymentservice.model.Payment;
import com.samilyak.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class StripeWebhookStubController {

    private final PaymentService paymentService;
    private final PaymentMessageProducer messageProducer;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping
    public ResponseEntity<String> handleFakeStripeEvent(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        log.info("📩 Webhook получен! payload={}, signature={}", payload, sigHeader);

        if (sigHeader == null || !sigHeader.equals(endpointSecret)) {
            log.warn("❌ Подпись не совпадает! sigHeader={} secret={}", sigHeader, endpointSecret);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        String eventType = (String) payload.get("type");
        String sessionId = (String) payload.get("sessionId");

        if (sessionId == null) {
            log.info("⚠️ Событие {} без sessionId, пропускаем.", eventType);
            return ResponseEntity.ok("ignored");
        }

        Payment payment = paymentService.findBySessionId(sessionId);

        switch (eventType) {
            case "checkout.session.completed" -> {
                log.info("✅ Оплата завершена успешно, sessionId={}", sessionId);

                String paymentIntentId = (String) payload.get("paymentIntentId");

                paymentService.updatePaymentWithIntent(
                        payment.getId(),
                        Payment.Status.PAID,
                        paymentIntentId
                );

                messageProducer.sendPaymentSuccess(new PaymentSuccessEvent(
                        payment.getBookingId(),
                        payment.getUserId(),
                        sessionId,
                        "user@example.com"
                ));
            }

            case "checkout.session.expired" -> {
                log.info("⚠️ Сессия оплаты истекла, sessionId={}", sessionId);

                paymentService.cancelPayment(String.valueOf(payment.getBookingId()));

                messageProducer.sendPaymentFailed(new PaymentFailedEvent(
                        payment.getBookingId(),
                        payment.getUserId(),
                        "Session expired"
                ));
            }

            default -> log.info("📌 Получено неизвестное событие: {}", eventType);
        }

        return ResponseEntity.ok("success");
    }
}

