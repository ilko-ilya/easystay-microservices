package com.samilyak.notification.dto;

import java.util.List;

public record NotificationResponse(

        String message,
        List<String> sentChannels

) {
}