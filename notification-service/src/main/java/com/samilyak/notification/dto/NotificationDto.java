package com.samilyak.notification.dto;

import java.io.Serializable;

public record NotificationDto(

        Long userId,
        String phoneNumber,
        String message

) implements Serializable {
}
