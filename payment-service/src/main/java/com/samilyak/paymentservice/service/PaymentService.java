package com.samilyak.paymentservice.service;

import com.samilyak.paymentservice.dto.PaymentRequestDto;
import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.model.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto request);

    PaymentResponseDto getPaymentById(UUID paymentId);

    void updatePaymentStatus(UUID paymentId, Payment.Status status);

    List<PaymentResponseDto> getPaymentsByUserId(Long userId);

    Payment findBySessionId(String sessionId);

}
