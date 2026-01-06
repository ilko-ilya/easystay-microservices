package com.samilyak.paymentservice.dto.event;

public record PaymentSuccessEvent(

        Long bookingId,
        Long userId,
        String paymentSessionId,
        String userEmailOrPhone

) {
}
