package com.samilyak.notification.controller;

import com.samilyak.notification.dto.NotificationRequest;
import com.samilyak.notification.event.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationPublisher publisher;  // Только publisher!

    @PostMapping
    public ResponseEntity<String> sendAsync(@RequestBody NotificationRequest request) {
        request.channels().forEach(channel ->
                publisher.publish(request.notification(), channel)
        );
        return ResponseEntity.accepted().build();
    }
}
