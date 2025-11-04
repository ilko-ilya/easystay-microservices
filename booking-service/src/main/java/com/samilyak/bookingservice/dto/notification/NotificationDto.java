package com.samilyak.bookingservice.dto.notification;

import java.io.Serializable;

public record NotificationDto(

        Long userId,
        String phoneNumber,
        String message

) implements Serializable {
}
