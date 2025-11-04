package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.dto.notification.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/send")
    void sendNotification(@RequestBody NotificationRequest requestDto);

}
