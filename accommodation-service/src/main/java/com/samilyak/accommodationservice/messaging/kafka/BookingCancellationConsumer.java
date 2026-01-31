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

    @KafkaListener(topics = "${application.kafka.topics.booking-cancellation-requested}")
    public void handleCancellation(BookingCancellationRequestedEvent event) {
        log.info("📩 Cancellation received: bookingId={}", event.bookingId());

        availabilityService.unlockDates(
                event.accommodationId(),
                event.checkInDate(),
                event.checkOutDate().minusDays(1)
        );

        messageProducer.sendDatesUnlocked(
                new DatesUnlockedEvent(
                        event.bookingId(),
                        event.accommodationId()
                )
        );
    }
}
