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

    @Override
    @CacheEvict(value = {"userBookings"}, key = "#authentication.name")
    public BookingResponseDto createBooking(BookingRequestDto requestDto, Authentication authentication) {
        Booking savedBooking = null;
        String token = getTokenFromAuthentication(authentication);
        String email = authentication.getName();

        // ✅ Валидация дат
        if (requestDto.checkInDate().isAfter(requestDto.checkOutDate()) ||
                requestDto.checkInDate().isEqual(requestDto.checkOutDate())) {
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }
        if (requestDto.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата заезда не может быть в прошлом");
        }

        // ✅ Получаем userId
        Long userId = userClient.getUserIdByEmail(email);
        if (userId == null) {
            throw new IllegalStateException("Не удалось получить userId для email: " + email);
        }

        String bearer = "Bearer " + token;

        try {
            // 1. 📌 Получаем жильё (с version)
            AccommodationDto accommodation = accommodationClient.getAccommodationById(
                    requestDto.accommodationId(), bearer
            );

            // 2. 🔒 Блокируем даты
            AccommodationLockResponse lockResponse = accommodationClient.lockDates(
                    requestDto.accommodationId(),
                    new AccommodationLockRequest(
                            requestDto.accommodationId(),
                            requestDto.checkInDate(),
                            requestDto.checkOutDate(),
                            accommodation.version()
                    ),
                    bearer
            );

            if (!lockResponse.success()) {
                throw new AccommodationNotAvailableException(lockResponse.message());
            }

            // 3. 💰 Считаем стоимость
            long days = ChronoUnit.DAYS.between(requestDto.checkInDate(), requestDto.checkOutDate());
            BigDecimal dailyRate = lockResponse.dailyRate() != null
                    ? lockResponse.dailyRate()
                    : accommodation.dailyRate();
            BigDecimal totalPrice = dailyRate.multiply(BigDecimal.valueOf(days));

            // 4. 📝 Создаём PENDING-бронирование (транзакция только для save)
            savedBooking = savePendingBooking(requestDto, userId, totalPrice);

            // 5. 💳 Создаём платёж
            PaymentRequestDto paymentRequest = new PaymentRequestDto(
                    savedBooking.getId(),
                    savedBooking.getTotalPrice(),
                    savedBooking.getPhoneNumber()
            );
            PaymentResponseDto paymentResponse = paymentClient.createPayment(paymentRequest, bearer);

            // 6. ✅ Подтверждаем бронь (сохраняем paymentId + CONFIRMED в одной транзакции)
            confirmBooking(savedBooking.getId(), paymentResponse.sessionId());

            // 7. 📩 Отправляем уведомление (асинхронно)
            NotificationDto notification = new NotificationDto(
                    userId,
                    requestDto.phoneNumber(),
                    "Создана сессия оплаты для бронирования #" + savedBooking.getId()
            );
            notificationProducer.sendNotification(notification, List.of("telegram", "sms"));

            return bookingMapper.toDto(getBooking(savedBooking.getId()));

        } catch (RuntimeException ex) {
            log.error("❌ Ошибка при создании брони: {}", ex.getMessage(), ex);

            // 🔄 Компенсация
            if (savedBooking != null) {
                compensate(savedBooking, requestDto, token);
            }
            throw ex;
        }
    }

    @Transactional
    protected Booking savePendingBooking(BookingRequestDto dto, Long userId, BigDecimal totalPrice) {
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

    @Transactional
    protected void confirmBooking(Long bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        booking.setPaymentId(paymentId);
        booking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    protected Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
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
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

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
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException("Can't find booking by bookingID: " + bookingId));
        Long userId = booking.getUserId();
        log.info("✅ Найден userId: {}", userId);
        return userId;
    }

    @CacheEvict(value = {"userBookings"}, key = "#authentication.name")
    @Override
    public void deleteBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

        bookingRepository.delete(booking);
    }

    @Override
    public Booking getLastBooking() {
        return bookingRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new EntityNotFoundException("No bookings found"));
    }

    private void compensate(Booking booking, BookingRequestDto dto, String token) {
        String bearer = "Bearer " + token;

        // Подтягиваем свежую версию из БД
        Booking currentBooking = bookingRepository.findById(booking.getId()).orElse(null);

        if (currentBooking == null) {
            log.warn("⚠️ Бронь {} не найдена для компенсации", booking.getId());
            return;
        }

        if (currentBooking.getStatus() == Booking.Status.CANCELED) {
            log.info("✅ Компенсация уже выполнена ранее для брони {}", booking.getId());
            return;
        }

        try {
            log.info("🔄 Запуск компенсации для брони {}", booking.getId());

            // 1. ✅ РАЗБЛОКИРОВКА ДАТ в accommodation-service (самое важное!)
            try {
                accommodationClient.unlockDates(
                        dto.accommodationId(),
                        new AccommodationLockRequest(
                                dto.accommodationId(),
                                dto.checkInDate(),
                                dto.checkOutDate(),
                                null // Версия не нужна для разблокировки
                        ),
                        bearer
                );
                log.info("✅ Даты разблокированы для accommodation {}", dto.accommodationId());
            } catch (Exception unlockEx) {
                log.warn("⚠️ Не удалось разблокировать даты: {}", unlockEx.getMessage());
            }

            // 2. ✅ ОТМЕНА ПЛАТЕЖА (если paymentId был создан)
            if (currentBooking.getPaymentId() != null) {
                try {
                    paymentClient.cancelPayment(currentBooking.getPaymentId());
                    log.info("✅ Платёж {} отменён", currentBooking.getPaymentId());
                } catch (Exception cancelEx) {
                    log.warn("⚠️ Не удалось отменить платёж {}: {}",
                            currentBooking.getPaymentId(), cancelEx.getMessage());
                }
            }

            // 3. ✅ ПОМЕЧАЕМ БРОНЬ КАК CANCELED
            currentBooking.setStatus(Booking.Status.CANCELED);
            bookingRepository.save(currentBooking);
            log.info("✅ Бронь {} помечена как CANCELED", currentBooking.getId());

            log.info("♻️ Компенсация выполнена успешно для брони {}", booking.getId());

        } catch (Exception e) {
            log.error("❌ Критическая ошибка при компенсации брони {}", booking.getId(), e);
            // Не пробрасываем исключение дальше, чтобы не маскировать оригинальную ошибку
        }
    }

    private String getTokenFromAuthentication(Authentication authentication) {
        if (authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        } else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
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
}

