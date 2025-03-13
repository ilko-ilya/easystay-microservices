package com.samilyak.paymentservice.dto;

import com.samilyak.paymentservice.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponseDto(

        UUID paymentId,
        Payment.Status status,
        String sessionUrl,
        String sessionId,
        BigDecimal amountToPay,
        String phoneNumber

) {
}
