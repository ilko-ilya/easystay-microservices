package com.samilyak.paymentservice.service;

import com.samilyak.paymentservice.client.BookingClient;
import com.samilyak.paymentservice.client.stripe.StripeClient;
import com.samilyak.paymentservice.dto.PaymentRequestDto;
import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.mapper.PaymentMapper;
import com.samilyak.paymentservice.model.Payment;
import com.samilyak.paymentservice.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeClient stripeClient;
    private final BookingClient bookingClient;

    @Transactional
    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        log.info("💳 Создание платежа для bookingId: {}, сумма: {}, номер телефона: {}",
                request.bookingId(), request.amountToPay(), request.phoneNumber());

        // Создаём сессию оплаты через Stripe
        Session session = stripeClient.createPaymentSession(request.amountToPay());
        log.info("✅ Создана платёжная сессия в Stripe: {}", session.getId());

        Long userId = bookingClient.getUserIdByBookingId(request.bookingId());

        if (userId == null) {
            throw new RuntimeException("Can't find userID by bookingID: " + request.bookingId());
        }

        Payment payment = Payment.builder()
                .bookingId(request.bookingId())
                .status(Payment.Status.PENDING)
                .amountToPay(request.amountToPay())
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .phoneNumber(request.phoneNumber())
                .userId(userId)
                .build();

        log.info("📌 PhoneNumber в PaymentServiceImpl перед сохранением: {}", request.phoneNumber());

        Payment savedPayment = paymentRepository.save(payment);
        log.info("✅ Платёж сохранён в БД: {}", savedPayment.getId());

        return paymentMapper.toDto(savedPayment);
    }

    @Transactional
    @Override
    public PaymentResponseDto getPaymentById(UUID paymentId) {
        log.info("🔍 Поиск платежа по ID: {}", paymentId);
        return paymentRepository.findById(paymentId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Платёж не найден: " + paymentId));
    }

    @Override
    public void updatePaymentStatus(UUID paymentId, Payment.Status status) {
        log.info("🔄 Обновление статуса платежа: {} -> {}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Платёж не найден: " + paymentId));

        payment.setStatus(status);
        paymentRepository.save(payment);
        log.info("✅ Статус платежа обновлён: {}", status);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        log.info("📊 Получение всех платежей пользователя: {}", userId);

        return paymentRepository.findAll().stream()
                .filter(payment -> {
                    Long bookingUserId = bookingClient.getUserIdByBookingId(payment.getBookingId());
                    return bookingUserId.equals(userId);
                })
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public Payment findBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found by sessionId" + sessionId));
    }
}
