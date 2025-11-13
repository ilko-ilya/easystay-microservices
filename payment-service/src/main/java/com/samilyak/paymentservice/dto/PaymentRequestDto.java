package com.samilyak.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentRequestDto(

        Long bookingId,
        BigDecimal amountToPay,
        String phoneNumber,
        Long userId

) {
}
