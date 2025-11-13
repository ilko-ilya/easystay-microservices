package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.model.AvailabilitySlot;
import com.samilyak.accommodationservice.repository.AvailabilitySlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationAvailabilityServiceImpl implements AccommodationAvailabilityService {

    private final AvailabilitySlotRepository slotRepository;

    @Transactional
    @Override
    public void initializeAvailabilitySlots(Long accommodationId, Integer availability) {
        log.info("Initializing {} availability slots for accommodation {}", availability, accommodationId);
        slotRepository.deleteByAccommodationId(accommodationId);

        slotRepository.flush();

        LocalDate start = LocalDate.now();
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (int i = 0; i < availability; i++) {
            slots.add(AvailabilitySlot.builder()
                    .accommodationId(accommodationId)
                    .date(start.plusDays(i))
                    .locked(false)
                    .build());
        }
        slotRepository.saveAll(slots);
    }

    @Transactional
    @Override
    public void updateAvailabilitySlots(Long accommodationId, Integer newAvailability) {
        log.info("Updating availability slots for accommodation {}", accommodationId);
        initializeAvailabilitySlots(accommodationId, newAvailability);
    }

    @Transactional
    @Override
    public boolean areDatesAvailable(Long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        log.info("📅 Проверка доступности жилья {} с {} по {}", accommodationId, checkIn, checkOut);

        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            log.warn("⚠️ Некорректный диапазон дат: {} - {}", checkIn, checkOut);
            return false;
        }

        List<AvailabilitySlot> slots =
                slotRepository.findByAccommodationIdAndDateBetween(accommodationId, checkIn, checkOut);

        if (slots.isEmpty()) {
            log.warn("⚠️ Нет слотов для жилья {} с {} по {} — пропускаем создание (слоты инициализируются отдельно)",
                    accommodationId, checkIn, checkOut);
            return false; // если нет — значит, даты нельзя забронировать
        }

        boolean available = slots.stream().noneMatch(AvailabilitySlot::isLocked);

        if (available) {
            log.info("✅ Жильё {} свободно для {} - {}", accommodationId, checkIn, checkOut);
        } else {
            log.warn("❌ Жильё {} занято для {} - {}", accommodationId, checkIn, checkOut);
        }

        return available;
    }

    @Transactional
    @Override
    public void lockDates(Long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        List<AvailabilitySlot> slots =
                slotRepository.findByAccommodationIdAndDateBetween(accommodationId, checkIn, checkOut);
        slots.forEach(slot -> slot.setLocked(true));
        slotRepository.saveAll(slots);
        log.info("Locked dates for accommodation {}: {} - {}", accommodationId, checkIn, checkOut);
    }

    @Transactional
    @Override
    public void unlockDates(Long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        List<AvailabilitySlot> slots =
                slotRepository.findByAccommodationIdAndDateBetween(accommodationId, checkIn, checkOut);
        slots.forEach(slot -> slot.setLocked(false));
        slotRepository.saveAll(slots);
        log.info("Unlocked dates for accommodation {}: {} - {}", accommodationId, checkIn, checkOut);
    }
}
