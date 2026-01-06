package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.client.AddressClient;
import com.samilyak.accommodationservice.dto.AccommodationDto;
import com.samilyak.accommodationservice.dto.AccommodationRequestDto;
import com.samilyak.accommodationservice.dto.AccommodationUpdateDto;
import com.samilyak.accommodationservice.dto.AddressResponseDto;
import com.samilyak.accommodationservice.exception.DatesNotAvailableException;
import com.samilyak.accommodationservice.exception.OptimisticLockingFailureException;
import com.samilyak.accommodationservice.mapper.AccommodationMapper;
import com.samilyak.accommodationservice.model.Accommodation;
import com.samilyak.accommodationservice.model.AvailabilitySlot;
import com.samilyak.accommodationservice.repository.AccommodationRepository;
import com.samilyak.accommodationservice.repository.AvailabilitySlotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AddressClient addressClient;
    private final AccommodationAvailabilityService availabilityService;
    private final AvailabilitySlotRepository slotRepository;

    @Transactional
    @Override
    public AccommodationDto create(AccommodationRequestDto requestDto) {
        log.info("🏠 Создание нового жилья: {}", requestDto);

        AddressResponseDto savedAddress = addressClient.createAddress(requestDto.location());
        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        accommodation.setAddressId(savedAddress.id());
        accommodation.setVersion(0L);

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        int daysToInitialize = requestDto.availability() != null ? requestDto.availability() : 365;
        availabilityService.initializeAvailabilitySlots(savedAccommodation.getId(), daysToInitialize);

        log.info("✅ Жильё ID={} успешно создано по адресу ID={}", savedAccommodation.getId(), savedAddress.id());
        return mapToDto(savedAccommodation);
    }

    @Transactional
    @Override
    public AccommodationDto update(Long id, AccommodationUpdateDto updateDto) {
        log.info("✏️ Обновление жилья ID={} данными {}", id, updateDto);
        Accommodation accommodation = getAccommodationOrThrow(id);

        if (updateDto.amenities() != null) {
            accommodation.setAmenities(updateDto.amenities());
        }
        if (updateDto.dailyRate() != null) {
            accommodation.setDailyRate(updateDto.dailyRate());
        }
        if (updateDto.availability() != null) {
            accommodation.setAvailability(updateDto.availability());
            availabilityService.updateAvailabilitySlots(id, updateDto.availability());
        }

        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);

        log.info("✏️ Обновление жилья ID={} данными {}", id, updateDto);
        return mapToDto(updatedAccommodation);
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations_list", key = "#pageable")
    public List<AccommodationDto> getAll() {
        log.info("📋 Получение всех доступных вариантов жилья");
        return accommodationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations", key = "#city")
    public List<AccommodationDto> getAccommodationsByCity(String city) {
        log.info("📌 Получаем адреса для города: {}", city);
        List<AddressResponseDto> addresses = addressClient.getAddressesByCity(city);

        List<Long> addressIds = addresses.stream()
                .map(AddressResponseDto::id)
                .toList();

        log.info("📌 Найденные ID адресов по городу: {}", addressIds);

        return accommodationRepository.findByAddressIdIn(addressIds)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations", key = "#country")
    public List<AccommodationDto> getAccommodationsByCountry(String country) {
        log.info("📌 Получаем адреса для страны: {}", country);
        List<AddressResponseDto> addresses = addressClient.getAddressesByCountry(country);

        List<Long> addressIds = addresses.stream()
                .map(AddressResponseDto::id)
                .toList();

        log.info("📌 Найдено {} адресов в стране {}", addressIds.size(), country);
        return accommodationRepository.findByAddressIdIn(addressIds)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations", key = "#id")
    public AccommodationDto getById(Long id) {
        log.info("🔍 Получение жилья по ID={}", id);
        Accommodation accommodation = getAccommodationOrThrow(id);

        return mapToDto(accommodation);
    }

    @Override
    public void deleteById(Long id) {
        log.info("🗑 Удаление жилья ID={}", id);

        if (!accommodationRepository.existsById(id)) {
            throw new EntityNotFoundException("Accommodation not found with id: " + id);
        }
        accommodationRepository.deleteById(id);
        log.info("✅ Жильё ID={} успешно удалено", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> getLockedDates(Long accommodationId) {
        return slotRepository.findByAccommodationIdAndLockedTrue(accommodationId)
                .stream()
                .map(AvailabilitySlot::getDate)
                .toList();
    }

    @Transactional
    @Override
    public void attemptReservation(Long accommodationId, LocalDate checkIn, LocalDate checkOut, Long expectedVersion) {
        log.info("🔒 SAGA: Попытка бронирования жилья {} с {} по {}", accommodationId, checkIn, checkOut);

        Accommodation accommodation = getAccommodationOrThrow(accommodationId);

        // 1. Проверка версионности (чтобы никто не перехватил перед носом)
        if (!accommodation.getVersion().equals(expectedVersion)) {
            throw new OptimisticLockingFailureException("Версия жилья устарела. Ожидалась: " + expectedVersion);
        }

        // 2. Проверка доступности
        if (!availabilityService.areDatesAvailable(accommodationId, checkIn, checkOut)) {
            throw new DatesNotAvailableException("Даты уже заняты");
        }

        // 3. Блокировка
        availabilityService.lockDates(accommodationId, checkIn, checkOut);

        // 4. Обновление версии
        accommodation.setVersion(accommodation.getVersion() + 1);
        accommodationRepository.save(accommodation);

        log.info("✅ Успешная блокировка SAGA для жилья {}", accommodationId);
    }

    private AccommodationDto mapToDto(Accommodation accommodation) {
        AddressResponseDto address = addressClient.getAddressById(accommodation.getAddressId());
        return new AccommodationDto(
                accommodation.getId(),
                accommodation.getType().name(),
                accommodation.getSize(),
                address,
                accommodation.getAmenities(),
                accommodation.getDailyRate(),
                accommodation.getAvailability(),
                accommodation.getVersion()
        );
    }

    private Accommodation getAccommodationOrThrow(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Accommodation not found with id: " + id));
    }
}
