package com.samilyak.bookingservice.controller;

import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Parameter;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Endpoints for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a booking",
            description = "Only registered users can create bookings")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<BookingResponseDto> create(
            @RequestBody @Valid BookingRequestDto requestDto,
            Authentication authentication
    ) {
        return ResponseEntity.status(CREATED).body(bookingService.createBooking(requestDto, authentication));
    }

    @Operation(summary = "Get user bookings",
            description = "Retrieve list of bookings for the authenticated user")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public List<BookingResponseDto> getUserBookings(Authentication authentication) {
        return bookingService.getUserBookings(authentication);
    }

    @Operation(summary = "Get booking by ID", description = "Retrieve booking details")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingById(id, authentication));
    }

    @Operation(summary = "Get userID by bookingID", description = "Get UserID by BookingID")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    @GetMapping("/{bookingId}/user-id")
    public ResponseEntity<Long> getUserIdByBookingId(@PathVariable Long bookingId) {
        log.info("📌 Получение userId для bookingId: {}", bookingId);
        return ResponseEntity.ok(bookingService.getUserIdByBookingId(bookingId));
    }

    @Operation(summary = "Delete booking", description = "Only MANAGER can delete a booking")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookingService.deleteBookingById(id);
    }

}
