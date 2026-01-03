package com.samilyak.paymentservice.service;

import com.samilyak.paymentservice.client.stripe.StripeClient;
import com.samilyak.paymentservice.dto.PaymentRequestDto;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeClient stripeClient;

    @Transactional
    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        log.info("💳 Создание платежа для бронирования {}", request.bookingId());

        // Создаём сессию оплаты через Stripe
        Session session = stripeClient.createPaymentSession(request.amountToPay());
        log.info("✅ Создана платёжная сессия в Stripe: {}", session.getId());

        Payment payment = Payment.builder()
                .bookingId(request.bookingId())
                .userId(request.userId())
                .status(Payment.Status.PENDING)
                .amountToPay(request.amountToPay())
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .phoneNumber(request.phoneNumber())
                .build();

        log.info("📌 Сохраняем платеж в БД: bookingId={}, phoneNumber={}",
                request.bookingId(), request.phoneNumber());

        Payment saved = paymentRepository.save(payment);

        log.info("✅ Платёж сохранён: {}", saved.getId());

        return paymentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponseDto getPaymentById(UUID paymentId) {
        log.info("🔍 Поиск платежа по ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Платёж не найден: " + paymentId));
    }

    @Override
    public void updatePaymentStatus(UUID paymentId, Payment.Status status) {
        log.info("🔄 Обновление статуса платежа: {} -> {}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Платёж не найден: " + paymentId));

        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("✅ Статус платежа обновлён: {}", status);
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
    public void cancelPayment(String paymentId) {
        log.info("❌ Cancel payment {}", paymentId);

        UUID uuid = parsePaymentId(paymentId);
        Payment payment = getPaymentById(uuid, paymentId);

        if (payment.getStatus() == Payment.Status.CANCELED) {
            log.info("⚠️ Payment already canceled");
            return;
        }

        if (payment.getPaymentIntentId() == null) {
            log.info("ℹ️ No paymentIntent, nothing to refund");
            payment.setStatus(Payment.Status.CANCELED);
            paymentRepository.save(payment);
            return;
        }

        stripeClient.refundPayment(payment.getPaymentIntentId());

        payment.setStatus(Payment.Status.CANCELED);
        paymentRepository.save(payment);

        log.info("✅ Payment {} refunded and canceled", paymentId);
    }

    @Override
    public void updatePaymentWithIntent(UUID paymentId,
                                        Payment.Status status,
                                        String paymentIntentId) {

        Payment payment = getPaymentById(paymentId, paymentId.toString());

        // Идемпотентность (на всякий случай)
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
