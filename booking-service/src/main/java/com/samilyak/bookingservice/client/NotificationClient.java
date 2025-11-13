package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.config.FeignTracingConfig;
import com.samilyak.bookingservice.dto.notification.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/notifications", configuration = FeignTracingConfig.class)
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestBody NotificationRequest requestDto);

}
