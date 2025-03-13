package com.samilyak.bookingservice.dto.client.notification;

public record NotificationRequestDto(

        Long userId,
        String phoneNumber,
        String message

) {
}
