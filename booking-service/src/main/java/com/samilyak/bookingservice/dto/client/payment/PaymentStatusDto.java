package com.samilyak.bookingservice.dto.client.payment;

public record PaymentStatusDto(

        Long bookingId,
        String status

) {
}
