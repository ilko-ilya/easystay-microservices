package com.samilyak.paymentservice.service;

import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.model.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponseDto getPaymentById(UUID paymentId);

    List<PaymentResponseDto> getPaymentsByUserId(Long userId);

    Payment findBySessionId(String sessionId);

    void cancelPayment(String bookingId);

    void updatePaymentWithIntent(UUID paymentId, Payment.Status status, String paymentIntentId);

    void initiatePayment(Long bookingId, Long userId, BigDecimal amount);

}
