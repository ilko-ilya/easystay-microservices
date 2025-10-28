package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.client.AccommodationClient;
import com.samilyak.bookingservice.client.NotificationClient;
import com.samilyak.bookingservice.client.PaymentClient;
import com.samilyak.bookingservice.client.UserClient;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockRequest;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockResponse;
import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.dto.accommodation.AccommodationDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentRequestDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentResponseDto;
import com.samilyak.bookingservice.dto.notification.NotificationDto;
import com.samilyak.bookingservice.exception.AccessDeniedException;
import com.samilyak.bookingservice.exception.AccommodationNotAvailableException;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.mapper.BookingMapper;
import com.samilyak.bookingservice.messaging.NotificationProducer;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final UserClient userClient;
    private final NotificationProducer notificationProducer;
    private final BookingCompensationService compensationService;

    @Override
    @CacheEvict(value = {"userBookings"}, key = "#authentication.name")
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto requestDto, Authentication authentication) {
        Booking savedBooking = null;
        String token = getTokenFromAuthentication(authentication);
        String email = authentication.getName();

        validateDates(requestDto);

        Long userId = userClient.getUserIdByEmail(email);
        if (userId == null) {
            throw new IllegalStateException("Не удалось получить userId для email: " + email);
        }

        try {
            // 1. 📌 Получаем жильё
            AccommodationDto accommodation = accommodationClient.getAccommodationById(
                    requestDto.accommodationId()
            );

            // 2. 🔒 Блокируем даты
            AccommodationLockResponse lockResponse = accommodationClient.lockDates(
                    requestDto.accommodationId(),
                    new AccommodationLockRequest(
                            requestDto.accommodationId(),
                            requestDto.checkInDate(),
                            requestDto.checkOutDate(),
                            accommodation.version()
                    )
            );

            if (!lockResponse.success()) {
                throw new AccommodationNotAvailableException(lockResponse.message());
            }

            // 3. 💰 Рассчитываем стоимость
            long days = ChronoUnit.DAYS.between(requestDto.checkInDate(), requestDto.checkOutDate());
            BigDecimal dailyRate = lockResponse.dailyRate() != null
                    ? lockResponse.dailyRate()
                    : accommodation.dailyRate();
            BigDecimal totalPrice = dailyRate.multiply(BigDecimal.valueOf(days));

            // 4. 📝 Создаем бронь PENDING
            savedBooking = savePendingBooking(requestDto, userId, totalPrice);

            // 5. 💳 Создаем платёж
            PaymentResponseDto paymentResponse = paymentClient.createPayment(
                    new PaymentRequestDto(
                            savedBooking.getId(),
                            savedBooking.getTotalPrice(),
                            savedBooking.getPhoneNumber()
                    )
            );

            // 6. ✅ Подтверждаем бронь
            savedBooking.setPaymentId(paymentResponse.sessionId());
            savedBooking.setStatus(Booking.Status.CONFIRMED);
            bookingRepository.save(savedBooking);

            // 7. 📩 Отправляем уведомление
            notificationProducer.sendNotification(
                    new NotificationDto(
                            userId,
                            requestDto.phoneNumber(),
                            "Создана сессия оплаты для бронирования #" + savedBooking.getId() +
                                    ". Пожалуйста, завершите оплату."
                    ),
                    List.of("telegram", "sms")
            );

            return bookingMapper.toDto(savedBooking);

        } catch (RuntimeException ex) {
            if (savedBooking != null) {
                compensationService.compensate(savedBooking, requestDto, token);
            }
            throw ex;
        }
    }

    @Cacheable(value = "userBookings", key = "#authentication.name", unless = "#result.size() == 0")
    @Override
    public List<BookingResponseDto> getUserBookings(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return bookingRepository.findAllByUserId(userId)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Cacheable(value = "booking", key = "#id + ':' + #authentication.name")
    @Override
    public BookingResponseDto getBookingById(Long id, Authentication authentication) {
        Booking booking = getBookingById(id);

        Long userId = getUserIdFromAuthentication(authentication);

        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to view this booking.");
        }
        return bookingMapper.toDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public Long getUserIdByBookingId(Long bookingId) {
        log.info("📌 Запрос на получение userId для bookingId = {}", bookingId);
        Booking booking = getBookingById(bookingId);
        Long userId = booking.getUserId();
        log.info("✅ Найден userId: {}", userId);
        return userId;
    }

    @CacheEvict(value = {"userBookings"}, key = "#authentication.name")
    @Override
    public void deleteBookingById(Long id) {
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
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }
        if (requestDto.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата заезда не может быть в прошлом");
        }
    }

    private String getTokenFromAuthentication(Authentication authentication) {
        if (authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        } else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            return authHeader != null ? authHeader.replace("Bearer ", "") : null;
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        log.info("🔍 Запрашиваем userId для email: {}", email);

        try {
            return userClient.getUserIdByEmail(email);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("User not found with email: " + email);
        } catch (FeignException e) {
            throw new RuntimeException("Error while fetching user ID from auth-service", e);
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

