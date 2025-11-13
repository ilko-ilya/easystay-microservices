package com.samilyak.accommodationservice.client;

import com.samilyak.accommodationservice.config.FeignTracingConfig;
import com.samilyak.accommodationservice.dto.AddressRequestDto;
import com.samilyak.accommodationservice.dto.AddressResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "address-service",
        path = "/api/addresses",
        configuration = FeignTracingConfig.class
)
public interface AddressClient {

    @GetMapping("/{id}")
    AddressResponseDto getAddressById(@PathVariable("id") Long id);

    @GetMapping("/batch")
    List<AddressResponseDto> getAddressesByIds(@RequestParam("ids") List<Long> ids);

    @PostMapping
    AddressResponseDto createAddress(@RequestBody AddressRequestDto addressRequestDto);

    @GetMapping("/ids")
    List<Long> getAddressIdsByCities(@RequestParam("cities") List<String> cities);

    @GetMapping("/search/by-country")
    List<AddressResponseDto> getAddressesByCountry(@RequestParam ("country") String country);

    @GetMapping("/search/by-city")
    List<AddressResponseDto> getAddressesByCity(@RequestParam ("city") String city);

}