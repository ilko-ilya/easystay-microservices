package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.client.AddressClient;
import com.samilyak.accommodationservice.dto.AccommodationDto;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AddressClient addressClient;

    @Override
    public AccommodationDto create(AccommodationRequestDto requestDto) {
        AddressResponseDto savedAddress = addressClient.createAddress(requestDto.location());

        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        accommodation.setAddressId(savedAddress.id());

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return mapToDto(savedAccommodation);
    }

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
        }

        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);

        return mapToDto(updatedAccommodation);
    }

    @Override
    @Cacheable(value = "accommodations_list", key = "#pageable")
    public List<AccommodationDto> getAll() {
        return accommodationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();

    }

    @Override
    @Cacheable(value = "accommodations", key = "#city")
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

    @Override
    @Cacheable(value = "accommodations", key = "#country")
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

    @Override
    @Cacheable(value = "accommodations", key = "#id")
    public AccommodationDto getById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Accommodation not found with id: " + id));

        return mapToDto(accommodation);
    }

    @Override
    public void deleteById(Long id) {
        accommodationRepository.deleteById(id);
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
                accommodation.getAvailability()
        );
    }
}
