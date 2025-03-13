package com.samilyak.notification.dto;

public record NotificationDto(

        Long userId,
        String phoneNumber,
        String message

) {
}
