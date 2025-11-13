package com.samilyak.bookingservice.dto.client.payment;

import java.math.BigDecimal;

public record PaymentRequestDto(

        Long bookingId,
        BigDecimal amountToPay,
        String phoneNumber,
        Long userId

) {
}
