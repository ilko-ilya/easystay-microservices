package com.samilyak.bookingservice.saga;

import com.samilyak.bookingservice.dto.event.BookingCancellationRequestedEvent;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.messaging.kafka.BookingMessageProducer;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingSagaService {

    private final BookingRepository bookingRepository;
    private final BookingMessageProducer bookingMessageProducer;

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with this id: " + bookingId));

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
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with this id: " + bookingId));

        booking.markPaymentCanceled();

        bookingRepository.save(booking);
    }

    public void handleDatesUnlocked(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with this id: " + bookingId));

        booking.markDatesUnlocked();

        bookingRepository.save(booking);
    }
}
