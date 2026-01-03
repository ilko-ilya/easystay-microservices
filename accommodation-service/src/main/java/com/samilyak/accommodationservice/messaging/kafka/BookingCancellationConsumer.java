package com.samilyak.accommodationservice.messaging.kafka;

import com.samilyak.accommodationservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.accommodationservice.dto.event.DatesUnlockedEvent;
import com.samilyak.accommodationservice.service.AccommodationAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCancellationConsumer {

    private final AccommodationAvailabilityService availabilityService;
    private final AccommodationMessageProducer messageProducer;

    @KafkaListener(
            topics = "${application.kafka.topics.booking-cancellation-requested}",
            groupId = "${spring.kafka.consumer.group-id}"
    )

    public void handleCancellation(BookingCancellationRequestedEvent event) {
        log.info(
                "📩 Cancellation received: bookingId={}, accommodationId={}, {} - {}",
                event.bookingId(),
                event.accommodationId(),
                event.checkInDate(),
                event.checkOutDate()
        );

        // 🔓 Разблокируем ровно те даты, которые были забронированы
        availabilityService.unlockDates(
                event.accommodationId(),
                event.checkInDate(),
                event.checkOutDate().minusDays(1) // ночи!
        );

        // 📤 Сообщаем booking-service
        messageProducer.sendDatesUnlocked(
                new DatesUnlockedEvent(
                        event.bookingId(),
                        event.accommodationId()
                )
        );
    }
}
