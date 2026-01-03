package com.samilyak.bookingservice.saga;

import com.samilyak.bookingservice.exception.EntityNotFoundException;
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

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with this id: " + bookingId));

        booking.startCancellation();

        bookingRepository.save(booking);
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
