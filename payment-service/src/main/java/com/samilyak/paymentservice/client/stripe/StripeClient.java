package com.samilyak.paymentservice.client.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeClient {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Создание платёжной сессии Stripe
     */
    public Session createPaymentSession(BigDecimal amount) {
        try {
            log.info("💳 Создание платёжной сессии в Stripe, сумма: {}", amount);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT) // Разовый платёж
                    .setSuccessUrl("http://localhost:3000/payment/success")
                    .setCancelUrl("http://localhost:3000/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe требует сумму в центах
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Booking Payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            log.info("✅ Платёжная сессия создана в Stripe: {}", session.getId());
            return session;

        } catch (StripeException e) {
            log.error("❌ Ошибка при создании платёжной сессии", e);
            throw new RuntimeException("Ошибка при создании платёжной сессии", e);
        }
    }

    public void refundPayment(String paymentIntentId) {
        try {
            log.info("💸 Stripe refund for paymentIntent {}", paymentIntentId);

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            Refund refund = Refund.create(params);
            log.info("✅ Stripe refund created: {}", refund.getId());

        } catch (StripeException e) {
            log.error("❌ Stripe refund failed", e);
            throw new RuntimeException("Stripe refund failed", e);
        }
    }

}