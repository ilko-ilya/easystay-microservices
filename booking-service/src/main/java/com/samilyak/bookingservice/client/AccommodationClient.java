package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.config.FeignServiceAuthConfig;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockRequest;
import com.samilyak.bookingservice.dto.accommodation.AccommodationLockResponse;
import com.samilyak.bookingservice.dto.accommodation.AccommodationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "accommodation-service", configuration = FeignServiceAuthConfig.class)
public interface AccommodationClient {

    @GetMapping("/api/accommodations/{id}")
    AccommodationDto getAccommodationById(@PathVariable("id") Long id);

    @GetMapping("/api/accommodations/{id}/availability")
    boolean isAccommodationAvailable(@PathVariable("id") Long id,
                                     @RequestParam("checkIn") LocalDate checkIn,
                                     @RequestParam("checkOut") LocalDate checkOut
    );

    // ✅ НОВЫЙ МЕТОД для атомарной блокировки дат
    @PostMapping("/api/accommodations/{id}/lock-dates")
    AccommodationLockResponse lockDates(
            @PathVariable("id") Long id,
            @RequestBody AccommodationLockRequest lockRequest
    );

    // ✅ НОВЫЙ МЕТОД для разблокировки дат (компенсация)
    @PostMapping("/api/accommodations/{id}/unlock-dates")
    ResponseEntity<Void> unlockDates(
            @PathVariable("id") Long id,
            @RequestBody AccommodationLockRequest unlockRequest
    );
}


