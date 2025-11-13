package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.client.AccommodationClient;
import com.samilyak.bookingservice.client.NotificationClient;
import com.samilyak.bookingservice.client.PaymentClient;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockRequest;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockResponse;
import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.dto.accommodation.AccommodationDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentRequestDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentResponseDto;
import com.samilyak.bookingservice.dto.notification.NotificationDto;
import com.samilyak.bookingservice.dto.notification.NotificationRequest;
import com.samilyak.bookingservice.exception.AccessDeniedException;
import com.samilyak.bookingservice.exception.AccommodationNotAvailableException;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.mapper.BookingMapper;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final PaymentClient paymentClient;
    private final BookingMapper bookingMapper;
    private final NotificationClient notificationClient;
    private final BookingCompensationService compensationService;

    @Override
    @CacheEvict(value = {"userBookings"}, key = "#userId")
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto requestDto, String userId, String role) {

        validateDates(requestDto);

        Booking savedBooking = null;
        boolean datesLocked = false;

        try {
            //  Проверяем, что жильё существует
            AccommodationDto accommodation = accommodationClient.getAccommodationById(
                    requestDto.accommodationId()
            );

            //  Проверяем, что даты свободны (в accommodation-service)
            boolean isAvailable = accommodationClient.isAccommodationAvailable(
                    requestDto.accommodationId(),
                    requestDto.checkInDate(),
                    requestDto.checkOutDate()
            );
            if (!isAvailable) {
                throw new AccommodationNotAvailableException("Даты уже заняты");
            }

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

            savedBooking = savePendingBooking(requestDto, userIdLong, totalPrice);

            //  Только теперь блокируем даты
            AccommodationLockResponse lockResponse = accommodationClient.lockDates(
                    requestDto.accommodationId(),
                    new AccommodationLockRequest(
                            requestDto.checkInDate(),
                            requestDto.checkOutDate(),
                            accommodation.version()
                    ),
                    userId
            );
            if (!lockResponse.success()) {
                throw new AccommodationNotAvailableException(lockResponse.message());
            }
            datesLocked = true;

            //  Создаем платёж
            PaymentResponseDto paymentResponse = paymentClient.createPayment(
                    new PaymentRequestDto(
                            savedBooking.getId(),
                            savedBooking.getTotalPrice(),
                            savedBooking.getPhoneNumber(),
                            Long.valueOf(userId)
                    )
            );

            //  Подтверждаем бронь
            savedBooking.setPaymentId(paymentResponse.sessionId());
            savedBooking.setStatus(Booking.Status.CONFIRMED);
            bookingRepository.save(savedBooking);

            //  Отправляем уведомление
            notificationClient.sendNotification(
                    new NotificationRequest(
                            new NotificationDto(
                                    userIdLong,
                                    requestDto.phoneNumber(),
                                    "Создана сессия оплаты для бронирования #" + savedBooking.getId() +
                                            ". Пожалуйста, завершите оплату."
                            ),
                            List.of("telegram", "sms")
                    )
            );

            return bookingMapper.toDto(savedBooking);

        } catch (RuntimeException ex) {
            //  При любой ошибке — откатываем и разблокируем даты
            if (savedBooking != null) {
                log.warn("↩️ Компенсация брони {}", savedBooking.getId());
                compensationService.compensate(savedBooking, requestDto);
            }

            // 2. Если бронь НЕ создана, но даты заблокированы → Unlock вручную
            else if (datesLocked) {
                try {
                    accommodationClient.unlockDates(
                            requestDto.accommodationId(),
                            new AccommodationLockRequest(
                                    requestDto.checkInDate(),
                                    requestDto.checkOutDate(),
                                    null
                            ),
                            userId
                    );
                    log.info("🔓 Даты разблокированы после ошибки");
                } catch (Exception unlockEx) {
                    log.error("⚠️ Ошибка при разблокировке дат: {}", unlockEx.getMessage());
                }
            }

            throw ex;
        }
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
