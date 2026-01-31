package com.samilyak.accommodationservice.messaging.kafka;

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

    @KafkaListener(topics = "${application.kafka.topics.booking-created}")
    public void onBookingCreated(BookingCreatedEvent event) { // 👈 Сразу DTO
        log.info("📨 Booking Created received: bookingId={}", event.bookingId());

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
            // Этот catch оставляем ТОЛЬКО для бизнес-ошибок (например, место уже занято),
            // чтобы отправить событие InventoryFailed.
            // Но если упадет сама база (ConnectionException), оно пролетит выше и вызовет ретрай.

            log.error("❌ Locking failed (Business Logic): {}", e.getMessage());
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
