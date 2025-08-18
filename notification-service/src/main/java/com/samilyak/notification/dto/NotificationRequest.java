package com.samilyak.notification.dto;

import java.util.List;

public record NotificationRequest(

        NotificationDto notification,
        List<String> channels

) {
}