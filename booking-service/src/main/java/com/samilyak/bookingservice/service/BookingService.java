package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto requestDto, Authentication authentication);

    List<BookingResponseDto> getUserBookings(Authentication authentication);

    BookingResponseDto getBookingById(Long id, Authentication authentication);

    Long getUserIdByBookingId(Long bookingId);

    void deleteBookingById(Long id);
}
