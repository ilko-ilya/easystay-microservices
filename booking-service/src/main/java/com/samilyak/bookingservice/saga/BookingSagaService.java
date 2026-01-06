package com.samilyak.bookingservice.saga;

import com.samilyak.bookingservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.messaging.kafka.BookingMessageProducer;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingSagaService {

    private final BookingRepository bookingRepository;
    private final BookingMessageProducer bookingMessageProducer;

    public void cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        booking.startCancellation();
        bookingRepository.save(booking);

        BookingCancellationRequestedEvent event =
                new BookingCancellationRequestedEvent(
                        booking.getId(),
                        booking.getAccommodationId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getPaymentId(),
                        booking.isRefundNeeded()
                );

        bookingMessageProducer.sendBookingCancellationRequested(event);
    }

    public void handlePaymentCanceled(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        booking.markPaymentCanceled();

        bookingRepository.save(booking);
        log.info("SAGA: Платёж отменен для брони {}", bookingId);

        checkCancellationComplete(booking);
    }

    public void handleDatesUnlocked(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        booking.markDatesUnlocked();

        bookingRepository.save(booking);
        log.info("SAGA: Даты разблокированы для брони {}", bookingId);

        checkCancellationComplete(booking);
    }

    //  ЛОГИКА СОЗДАНИЯ (То, что нужно добавить для clean BookingService)

    public void finalizeBookingCreation(Long bookingId, String paymentSessionId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() == Booking.Status.PENDING) {
            booking.setStatus(Booking.Status.CONFIRMED);
            booking.setPaymentId(paymentSessionId);
            bookingRepository.save(booking);
            log.info("✅ SAGA: Бронь {} успешно ПОДТВЕРЖДЕНА (Оплата прошла)", bookingId);
        } else {
            // Этот лог полезен для отладки, если вдруг пришел дубликат события
            log.warn("⚠️ SAGA: Игнорируем подтверждение для брони {}, так как статус уже {}",
                    bookingId, booking.getStatus());
        }
    }

    /**
     * Вызывается, когда Accommodation (нет мест) или Payment (нет денег) прислали отказ.
     */
    public void failBookingCreation(Long bookingId, String reason) {
        Booking booking = getBookingOrThrow(bookingId);

        log.warn("🛑 SAGA: Ошибка создания брони {}. Причина: {}", bookingId, reason);

        // Переводим в CANCELED, если она еще "жива"
        if (booking.getStatus() == Booking.Status.PENDING) {
            booking.setStatus(Booking.Status.CANCELED);
            bookingRepository.save(booking);
        }
    }

    private void checkCancellationComplete(Booking booking) {
        if (booking.isPaymentCanceled() && booking.isDatesUnlocked()) {
            booking.setStatus(Booking.Status.CANCELED);
            bookingRepository.save(booking);
            log.info("SAGA: Бронь {} полностью ОТМЕНЕНА", booking.getId());
        }
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
    }
}
