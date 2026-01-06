package com.samilyak.accommodationservice.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samilyak.accommodationservice.dto.event.BookingCreatedEvent;
import com.samilyak.accommodationservice.dto.event.InventoryReservationFailedEvent;
import com.samilyak.accommodationservice.dto.event.InventoryReservedEvent;
import com.samilyak.accommodationservice.service.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCreationConsumer {

    private final AccommodationService accommodationService;
    private final AccommodationMessageProducer messageProducer;
    private final ObjectMapper objectMapper; // 👇 Наш инструмент

    @KafkaListener(
            topics = "${application.kafka.topics.booking-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onBookingCreated(String message) { // 👈 Принимаем String!
        log.info("📨 RAW MESSAGE received: {}", message); // Увидим текст до ошибки!

        BookingCreatedEvent event;
        try {
            // 👇 Сами превращаем текст в объект. Если упадет - увидим почему.
            event = objectMapper.readValue(message, BookingCreatedEvent.class);
        } catch (Exception e) {
            log.error("❌ JSON Parse Error: {}", e.getMessage());
            return; // Не можем прочитать - выходим
        }

        log.info("✅ Parsed Event: bookingId={}, dates={} - {}",
                event.bookingId(), event.checkInDate(), event.checkOutDate());

        try {
            accommodationService.attemptReservation(
                    event.accommodationId(),
                    event.checkInDate(),
                    event.checkOutDate(),
                    event.accommodationVersion()
            );

            log.info("✅ Accommodation locked. Sending success event.");
            messageProducer.sendInventoryReserved(
                    new InventoryReservedEvent(
                            event.bookingId(),
                            event.userId(),
                            event.totalPrice(),
                            event.phoneNumber()
                    )
            );

        } catch (Exception e) {
            log.error("❌ Locking failed for booking {}: {}", event.bookingId(), e.getMessage());
            messageProducer.sendInventoryFailed(
                    new InventoryReservationFailedEvent(
                            event.bookingId(),
                            event.userId(),
                            e.getMessage()
                    )
            );
        }
    }
}
