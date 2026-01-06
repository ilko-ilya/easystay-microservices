package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.config.FeignTracingConfig;
import com.samilyak.bookingservice.dto.accommodation.AccommodationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "accommodation-service", configuration = FeignTracingConfig.class)
public interface AccommodationClient {

    @GetMapping("/api/accommodations/{id}")
    AccommodationDto getAccommodationById(@PathVariable("id") Long id);

}
