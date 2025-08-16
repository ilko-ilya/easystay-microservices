package com.samilyak.bookingservice.dto.notification;

public record NotificationDto(

        Long userId,
        String phoneNumber,
        String message

) {
}
