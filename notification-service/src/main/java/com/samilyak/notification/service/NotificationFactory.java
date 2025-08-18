package com.samilyak.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationFactory {

    private final List<NotificationSender> senders;

    public NotificationSender getSender(String channelType) {
        return senders.stream()
                .filter(s -> s.getChannelType().equals(channelType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported channel: " + channelType));
    }
}
