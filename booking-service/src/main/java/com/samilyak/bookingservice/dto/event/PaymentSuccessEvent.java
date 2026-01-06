package com.samilyak.bookingservice.dto.event;

public record PaymentSuccessEvent(

        Long bookingId,
        Long userId,
        String paymentSessionId,
        String userEmailOrPhone

) {
}
