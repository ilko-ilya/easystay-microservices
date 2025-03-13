package com.samilyak.accommodationservice.service;

import com.samilyak.accommodationservice.dto.AccommodationDto;
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

}