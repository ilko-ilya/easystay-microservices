package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.model.Booking;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto requestDto, String userId, String role);

    List<BookingResponseDto> getUserBookings(String userId);

    BookingResponseDto getBookingById(Long id, String userId, String role);

    Long getUserIdByBookingId(Long bookingId);

    void deleteBookingById(Long id, String role);

    Booking getLastBooking();
}
