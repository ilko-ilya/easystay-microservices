package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.client.AccommodationClient;
import com.samilyak.bookingservice.client.NotificationClient;
import com.samilyak.bookingservice.client.PaymentClient;
import com.samilyak.bookingservice.client.UserClient;
import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.dto.client.accommodation.AccommodationDto;
import com.samilyak.bookingservice.dto.client.notification.NotificationRequestDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentRequestDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentResponseDto;
import com.samilyak.bookingservice.exception.AccessDeniedException;
import com.samilyak.bookingservice.exception.AccommodationNotAvailableException;
import com.samilyak.bookingservice.exception.EntityNotFoundException;
import com.samilyak.bookingservice.mapper.BookingMapper;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AccommodationClient accommodationClient;
    private final PaymentClient paymentClient;
    private final BookingMapper bookingMapper;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    @CacheEvict(value = {"userBookings"}, key = "#authentication.name")
    @Override
    public BookingResponseDto createBooking(BookingRequestDto requestDto, Authentication authentication) {
        String email = authentication.getName();
        String token = authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest()
                .getHeader(AUTHORIZATION);

        log.info("Итоговый токен: {}", token);
        log.info("Запрашиваем userId для email: {}", email);
        Long userId = userClient.getUserIdByEmail(email);

        if (userId == null) {
            throw new IllegalStateException("Не удалось получить userId для email: " + email);
        }

        log.info("Запрашиваем жильё по id: {}", requestDto.accommodationId());
        AccommodationDto accommodation = accommodationClient.getAccommodationById(
                requestDto.accommodationId(),
                "Bearer " + token
        );

        if (!bookingRepository.findOverlappingBookings(
                requestDto.accommodationId(),
                requestDto.checkInDate(),
                requestDto.checkOutDate()
        ).isEmpty()) {
            throw new AccommodationNotAvailableException("Accommodation is not available for these dates.");
        }

        long days = ChronoUnit.DAYS.between(requestDto.checkInDate(), requestDto.checkOutDate());
        BigDecimal totalPrice = accommodation.dailyRate().multiply(BigDecimal.valueOf(days));
        log.info("💰 Рассчитанная цена за {} дней: {}", days, totalPrice);

        Booking booking = bookingMapper.toModel(requestDto, userId);
        booking.setStatus(Booking.Status.PENDING);
        booking.setTotalPrice(totalPrice);
        Booking savedBooking = bookingRepository.save(booking);

        // ✅ Создаём платёж
        PaymentRequestDto paymentRequest = new PaymentRequestDto(
                savedBooking.getId(),
                savedBooking.getTotalPrice(),
                savedBooking.getPhoneNumber()
        );

        log.info("📞 Отправляем phoneNumber в PaymentService: {}", requestDto.phoneNumber());

        PaymentResponseDto paymentResponse = paymentClient.createPayment(paymentRequest, "Bearer " + token);
        savedBooking.setPaymentId(paymentResponse.sessionId());
        savedBooking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(savedBooking);

        // ✅ Отправляем уведомление через Feign-клиент
        log.info("📢 Отправляем уведомление об успешном платеже...");
        NotificationRequestDto notificationRequest = new NotificationRequestDto(
                userId,
                requestDto.phoneNumber(),
                "Оплата прошла успешно! Ваше бронирование подтверждено."
        );

        notificationClient.sendNotification(notificationRequest);

        return bookingMapper.toDto(savedBooking);
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

