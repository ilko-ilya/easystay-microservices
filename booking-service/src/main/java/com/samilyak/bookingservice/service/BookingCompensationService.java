package com.samilyak.bookingservice.service;

import com.samilyak.bookingservice.client.AccommodationClient;
import com.samilyak.bookingservice.client.PaymentClient;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockRequest;
import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.model.Booking;
import com.samilyak.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCompensationService {

    private final BookingRepository bookingRepository;
    private final AccommodationClient accommodationClient;
    private final PaymentClient paymentClient;

    public void compensate(Booking booking, BookingRequestDto dto) {
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

            // 1. Разблокировка дат
            try {
                accommodationClient.unlockDates(
                        dto.accommodationId(),
                        new AccommodationLockRequest(
                                dto.accommodationId(),
                                dto.checkInDate(),
                                dto.checkOutDate(),
                                null
                        )
                );
                log.info("✅ Даты разблокированы для accommodation {}", dto.accommodationId());
            } catch (Exception unlockEx) {
                log.warn("⚠️ Не удалось разблокировать даты: {}", unlockEx.getMessage());
            }

            // 2. Отмена платежа
            if (currentBooking.getPaymentId() != null) {
                try {
                    paymentClient.cancelPayment(currentBooking.getPaymentId());
                    log.info("✅ Платёж {} отменён", currentBooking.getPaymentId());
                } catch (Exception cancelEx) {
                    log.warn("⚠️ Не удалось отменить платёж {}: {}",
                            currentBooking.getPaymentId(), cancelEx.getMessage());
                }
            }

            // 3. Отмечаем бронь как CANCELED
            currentBooking.setStatus(Booking.Status.CANCELED);
            bookingRepository.save(currentBooking);
            log.info("✅ Бронь {} помечена как CANCELED", currentBooking.getId());

            log.info("♻️ Компенсация выполнена успешно для брони {}", booking.getId());

        } catch (Exception e) {
            log.error("❌ Критическая ошибка при компенсации брони {}", booking.getId(), e);
        }
    }
}
