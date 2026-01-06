package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.client.AccommodationClient;
import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.dto.accommodation.AccommodationDto;
import com.samilyak.bookingservice.dto.event.BookingCreatedEvent;
import com.samilyak.bookingservice.dto.notification.NotificationDto;
import com.samilyak.bookingservice.exception.AccessDeniedException;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.mapper.BookingMapper;
import com.samilyak.bookingservice.messaging.NotificationProducer;
import com.samilyak.bookingservice.messaging.kafka.BookingMessageProducer;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationClient accommodationClient;
    private final BookingMapper bookingMapper;
    private final NotificationProducer notificationProducer;
    private final BookingMessageProducer messageProducer;

    @Override
    @CacheEvict(value = {"userBookings"}, key = "#userId")
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto requestDto, String userId, String role) {
        log.info("🔥🔥🔥 ВЕРСИЯ 2.0 - ПРОВЕРКА! Время: {}", java.time.LocalDateTime.now());

        validateDates(requestDto);

        AccommodationDto accommodation = accommodationClient.getAccommodationById(
                requestDto.accommodationId()
        );

        //  Рассчитываем стоимость
        long days = ChronoUnit.DAYS.between(requestDto.checkInDate(), requestDto.checkOutDate());
        BigDecimal dailyRate = accommodation.dailyRate();
        BigDecimal totalPrice = dailyRate.multiply(BigDecimal.valueOf(days));

        //  Создаем бронь PENDING
        Long userIdLong = null;
        try {
            userIdLong = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.warn("UserId '{}' is not numeric — using null instead", userId);
        }

        Booking savedBooking = savePendingBooking(requestDto, userIdLong, totalPrice);

        log.info("Booking #{} saved with status PENDING. Starting SAGA...", savedBooking.getId());

        BookingCreatedEvent event = new BookingCreatedEvent(
                savedBooking.getId(),
                userIdLong,
                requestDto.accommodationId(),
                requestDto.checkInDate(),
                requestDto.checkOutDate(),
                savedBooking.getTotalPrice(),
                requestDto.phoneNumber(),
                accommodation.version()
        );

        messageProducer.sendBookingCreated(event);

        NotificationDto notification = new NotificationDto(
                userIdLong,
                requestDto.phoneNumber(),
                "Ваше бронирование #" + savedBooking.getId() + " создано и ожидает оплаты."
        );

        notificationProducer.sendNotification(
                notification,
                List.of("telegram", "email")
        );

        return bookingMapper.toDto(savedBooking);
    }

    @Cacheable(value = "userBookings", key = "#userId", unless = "#result.size() == 0")
    @Override
    public List<BookingResponseDto> getUserBookings(String userId) {
        return bookingRepository.findAllByUserId(Long.valueOf(userId))
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "booking", key = "#id + ':' + #userId")
    @Override
    public BookingResponseDto getBookingById(Long id, String userId, String role) {
        Booking booking = getBookingById(id);

        if (!booking.getUserId().equals(Long.valueOf(userId)) && !"MANAGER".equals(role)) {
            throw new AccessDeniedException("You are not authorized to view this booking.");
        }
        return bookingMapper.toDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public Long getUserIdByBookingId(Long bookingId) {
        log.info("📌 Request for receipt userId for bookingId = {}", bookingId);
        Booking booking = getBookingById(bookingId);
        Long userId = booking.getUserId();
        log.info("✅ Found userId: {}", userId);
        return userId;
    }

    @CacheEvict(value = {"userBookings"}, key = "#role")
    @Override
    public void deleteBookingById(Long id, String role) {
        if (!"MANAGER".equals(role)) {
            throw new AccessDeniedException("Only MANAGER can delete bookings.");
        }
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    @Override
    public Booking getLastBooking() {
        return bookingRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new EntityNotFoundException("No bookings found"));
    }

    private void validateDates(BookingRequestDto requestDto) {
        if (requestDto.checkInDate().isAfter(requestDto.checkOutDate()) ||
                requestDto.checkInDate().isEqual(requestDto.checkOutDate())) {
            throw new IllegalArgumentException("Incorrect booking dates");
        }
        if (requestDto.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The arrival date cannot be in the past");
        }
    }

    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }

    private Booking savePendingBooking(BookingRequestDto dto, Long userId, BigDecimal totalPrice) {
        Booking booking = new Booking();
        booking.setAccommodationId(dto.accommodationId());
        booking.setUserId(userId);
        booking.setCheckInDate(dto.checkInDate());
        booking.setCheckOutDate(dto.checkOutDate());
        booking.setPhoneNumber(dto.phoneNumber());
        booking.setStatus(Booking.Status.PENDING);
        booking.setTotalPrice(totalPrice);
        return bookingRepository.save(booking);
    }
}
