package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.client.AddressClient;
import com.samilyak.accommodationservice.dto.AccommodationDto;
import com.samilyak.accommodationservice.dto.AccommodationLockCommand;
import com.samilyak.accommodationservice.dto.AccommodationLockResult;
import com.samilyak.accommodationservice.dto.AccommodationRequestDto;
import com.samilyak.accommodationservice.dto.AccommodationUpdateDto;
import com.samilyak.accommodationservice.dto.AddressResponseDto;
import com.samilyak.accommodationservice.mapper.AccommodationMapper;
import com.samilyak.accommodationservice.model.Accommodation;
import com.samilyak.accommodationservice.repository.AccommodationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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

    @Transactional
    @Override
    public AccommodationDto create(AccommodationRequestDto requestDto) {
        AddressResponseDto savedAddress = addressClient.createAddress(requestDto.location());

        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        accommodation.setAddressId(savedAddress.id());
        accommodation.setVersion(0L);

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        availabilityService.initializeAvailabilitySlots(savedAccommodation.getId(),
                requestDto.availability() != null ? requestDto.availability() : 1);

        return mapToDto(savedAccommodation);
    }

    @Transactional
    @Override
    public AccommodationDto update(Long id, AccommodationUpdateDto updateDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Accommodation not found with id: " + id));

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
        return mapToDto(updatedAccommodation);
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations_list", key = "#pageable")
    public List<AccommodationDto> getAll() {
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
        log.info("📌 Найденные ID адресов по стране: {}", addressIds);

        return accommodationRepository.findByAddressIdIn(addressIds)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
//    @Cacheable(value = "accommodations", key = "#id")
    public AccommodationDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Accommodation not found with id: " + id));

        return mapToDto(accommodation);
    }

    @Override
    public void deleteById(Long id) {
        accommodationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(Long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        return availabilityService.areDatesAvailable(accommodationId, checkIn, checkOut);
    }

    @Transactional
    @Override
    public AccommodationLockResult lockDates(Long accommodationId, AccommodationLockCommand command) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new EntityNotFoundException("Жилье не найдено с id: " + accommodationId));

        if (!accommodation.getVersion().equals(command.expectedVersion())) {
            log.warn("Версия устарела для жилья {}. Ожидалось: {}, актуальная: {}",
                    accommodationId, command.expectedVersion(), accommodation.getVersion());
            return new AccommodationLockResult(false, "Данные устарели. Обновите страницу", null);
        }

        if (!availabilityService.areDatesAvailable(accommodationId, command.checkInDate(), command.checkOutDate())) {
            log.warn("Даты уже заняты для жилья {}: с {} по {}",
                    accommodationId, command.checkInDate(), command.checkOutDate());
            return new AccommodationLockResult(false, "Даты уже заняты", null);
        }

        try {
            availabilityService.lockDates(accommodationId, command.checkInDate(), command.checkOutDate());
            accommodation.setVersion(accommodation.getVersion() + 1);
            accommodationRepository.save(accommodation);

            log.info("Даты заблокированы для жилья {}: с {} по {}",
                    accommodationId, command.checkInDate(), command.checkOutDate());

            return new AccommodationLockResult(true, "Даты заблокированы", accommodation.getDailyRate());

        } catch (Exception e) {
            log.error("Ошибка при блокировке дат для жилья {}: {}", accommodationId, e.getMessage());
            return new AccommodationLockResult(false, "Ошибка при блокировке дат: " + e.getMessage(), null);
        }
    }

    @Transactional
    @Override
    public void unlockDates(Long accommodationId, AccommodationLockCommand command) {
        try {
            availabilityService.unlockDates(accommodationId, command.checkInDate(), command.checkOutDate());
            log.info("Даты разблокированы для жилья {}: с {} по {}",
                    accommodationId, command.checkInDate(), command.checkOutDate());
        } catch (Exception e) {
            log.error("Ошибка при разблокировке дат для жилья {}: {}", accommodationId, e.getMessage());
        }
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
}
