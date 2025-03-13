package com.samilyak.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramNotificationService {

    private final String botToken;
    private final String chatId;
    private final String botUsername;
    private final RestTemplate restTemplate;

    public TelegramNotificationService(
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId,
            @Value("${telegram.bot-username}") String botUsername) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.botUsername = botUsername;
        this.restTemplate = new RestTemplate();
    }

    public void sendNotification(String message) {
        String formattedMessage = String.format(
                "🤖 %s\n%s",
                botUsername, message
        );

        String telegramApiUrl = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                botToken, chatId, formattedMessage);

        restTemplate.getForObject(telegramApiUrl, String.class);
    }

}
