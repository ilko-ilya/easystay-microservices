package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.dto.client.accommodation.AccommodationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "accommodation-service")
public interface AccommodationClient {

    @GetMapping("/api/accommodations/{id}")
    AccommodationDto getAccommodationById(
            @PathVariable ("id") Long id,
            @RequestHeader(AUTHORIZATION) String token
    );

    @GetMapping("/api/accommodations/{id}/availability")
    boolean isAccommodationAvailable(@PathVariable ("id") Long id,
                                     @RequestParam ("checkIn") LocalDate checkIn,
                                     @RequestParam ("checkOut") LocalDate checkOut);
}
