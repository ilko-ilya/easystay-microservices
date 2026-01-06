package com.samilyak.paymentservice.service;

import com.samilyak.paymentservice.client.stripe.StripeClient;
import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.exception.EntityNotFoundException;
import com.samilyak.paymentservice.mapper.PaymentMapper;
import com.samilyak.paymentservice.model.Payment;
import com.samilyak.paymentservice.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.samilyak.paymentservice.model.Payment.Status.CANCELED;
import static com.samilyak.paymentservice.model.Payment.Status.PENDING;
import static com.samilyak.paymentservice.model.Payment.Status.REFUNDED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeClient stripeClient;

    @Transactional
    @Override
    public void initiatePayment(Long bookingId, Long userId, BigDecimal amount) {
        log.info("🚀 Инициация платежа для bookingId={}", bookingId);

        // 1. Идемпотентность
        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            log.warn("⚠️ Платеж для брони {} уже существует.", bookingId);
            return;
        }

        // 2. Stripe Session
        Session session = stripeClient.createPaymentSession(amount);

        // 3. Сохраняем (PENDING)
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .userId(userId)
                .amountToPay(amount)
                .status(PENDING)
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();

        paymentRepository.save(payment);
        log.info("💾 Платеж создан: BookingID={}, Status=PENDING", bookingId);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponseDto getPaymentById(UUID paymentId) {
        log.info("🔍 Поиск платежа по ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Платёж не найден: " + paymentId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        log.info("📊 Получение всех платежей пользователя: {}", userId);

        return paymentRepository.findAllByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Payment findBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found by sessionId: " + sessionId));
    }

    @Transactional
    @Override
    public void cancelPayment(String bookingIdStr) {
        Long bookingId = Long.valueOf(bookingIdStr);
        log.info("🔄 Запрос на отмену платежа для bookingId={}", bookingId);

        // 2. Ищем платеж по ID БРОНИРОВАНИЯ
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking: " + bookingId));

        // 3. Если уже отменен — выходим
        if (payment.getStatus() == CANCELED || payment.getStatus() == REFUNDED) {
            log.warn("⚠️ Платеж для брони {} уже отменен.", bookingId);
            return;
        }

        // 4. ЛОГИКА ВОЗВРАТА (Гибридная)
        if (payment.getPaymentIntentId() != null) {
            // Если Stripe уже провел оплату
            log.info("💰 Выполняем возврат средств через Stripe (Intent: {})...", payment.getPaymentIntentId());
            stripeClient.refundPayment(payment.getPaymentIntentId());

            payment.setStatus(REFUNDED);
            log.info("✅ Средства возвращены. Статус REFUNDED.");
        } else {
            // Если оплаты не было (PENDING или ошибка)
            log.info("ℹ️ PaymentIntent отсутствует (клиент не платил). Просто отменяем статус.");
            payment.setStatus(CANCELED);
        }

        paymentRepository.save(payment);
    }

    @Override
    public void updatePaymentWithIntent(UUID paymentId,
                                        Payment.Status status,
                                        String paymentIntentId) {

        Payment payment = getPaymentById(paymentId, paymentId.toString());

        if (payment.getStatus() == status
                && Objects.equals(payment.getPaymentIntentId(), paymentIntentId)) {
            return;
        }

        payment.setStatus(status);
        payment.setPaymentIntentId(paymentIntentId);

        paymentRepository.save(payment);

        log.info(
                "✅ Payment {} updated: status={}, paymentIntentId={}",
                paymentId, status, paymentIntentId
        );
    }

    private UUID parsePaymentId(String paymentId) {
        try {
            return UUID.fromString(paymentId);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid paymentId format: " + paymentId);
        }
    }

    private Payment getPaymentById(UUID uuid, String paymentId) {
        return paymentRepository.findById(uuid)
                .orElseThrow(() ->
                        new EntityNotFoundException("Платёж не найден: " + paymentId)
                );
    }

}
