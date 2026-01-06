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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping
    public ResponseEntity<BookingResponseDto> create(
            @RequestBody @Valid BookingRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        log.info("📦 Создание брони пользователем userId={}, role={}", userId, role);
        return ResponseEntity.status(CREATED)
                .body(bookingService.createBooking(requestDto, userId, role));    }

    @Operation(summary = "Get user bookings",
            description = "Retrieve list of bookings for the authenticated user")
    @GetMapping("/my")
    public List<BookingResponseDto> getUserBookings(@RequestHeader("X-User-Id") String userId) {
        return bookingService.getUserBookings(userId);
    }

    @Operation(summary = "Get booking by ID", description = "Retrieve booking details")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(bookingService.getBookingById(id, userId, role));
    }

    @Operation(summary = "Get userID by bookingID", description = "Get UserID by BookingID")
    @GetMapping("/{bookingId}/user-id")
    public ResponseEntity<Long> getUserIdByBookingId(@PathVariable("bookingId") Long bookingId) {
        log.info("📌 Получение userId для bookingId: {}", bookingId);
        return ResponseEntity.ok(bookingService.getUserIdByBookingId(bookingId));
    }

    @Operation(summary = "Delete booking", description = "Only MANAGER can delete a booking")
    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id, @RequestHeader("X-User-Role") String role) {
        bookingService.deleteBookingById(id, role);
    }

}
