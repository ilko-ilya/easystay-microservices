package com.samilyak.notification.service;

import com.samilyak.notification.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class TelegramNotificationService implements NotificationSender {

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

    @Override
    public String getChannelType() {
        return "telegram";
    }

    @Override
    public void send(NotificationDto dto) {
        try {
            String message = formatMessage(dto);
            String url = buildTelegramUrl(message);

            restTemplate.getForObject(url, String.class);
            log.info("🤖 Telegram уведомление отправлено: {}", dto);
        } catch (Exception e) {
            log.error("❌ Ошибка отправки Telegram: {}", e.getMessage());
        }
    }

    private String formatMessage(NotificationDto dto) {
        return String.format(
                "🤖 %s\n📢 Новое бронирование!\n👤 ID: %s\n📞 Телефон: %s\n💬 %s",
                botUsername, dto.userId(), dto.phoneNumber(), dto.message()
        );
    }

    private String buildTelegramUrl(String message) {
        return String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                botToken,
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8)
        );
    }
}
