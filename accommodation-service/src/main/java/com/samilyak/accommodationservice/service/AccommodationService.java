package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.dto.AccommodationDto;
import com.samilyak.accommodationservice.dto.AccommodationLockCommand;
import com.samilyak.accommodationservice.dto.AccommodationLockResult;
import com.samilyak.accommodationservice.dto.AccommodationRequestDto;
import com.samilyak.accommodationservice.dto.AccommodationUpdateDto;

import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    AccommodationDto create(AccommodationRequestDto requestDto);

    AccommodationDto update(Long id, AccommodationUpdateDto updateDto);

    List<AccommodationDto> getAll();

    List<AccommodationDto> getAccommodationsByCity(String city);

    List<AccommodationDto> getAccommodationsByCountry(String country);

    AccommodationDto getById(Long id);

    void deleteById(Long id);

    //  Новые методы для работы с доступностью
    boolean isAvailable(Long accommodationId, LocalDate checkIn, LocalDate checkOut);

    AccommodationLockResult lockDates(Long accommodationId, AccommodationLockCommand command);

    void unlockDates(Long accommodationId, AccommodationLockCommand command);

    List<LocalDate> getLockedDates(Long accommodationId);

}