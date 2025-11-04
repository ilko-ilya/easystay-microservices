package com.samilyak.bookingservice.dto.notification;

import java.util.List;

public record NotificationRequest(

        NotificationDto notification,
        List<String> channels

) {
}
