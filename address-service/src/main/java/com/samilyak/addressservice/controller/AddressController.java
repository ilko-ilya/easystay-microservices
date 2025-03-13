package com.samilyak.addressservice.controller;

import com.samilyak.addressservice.dto.AddressRequestDto;
import com.samilyak.addressservice.dto.AddressResponseDto;
import com.samilyak.addressservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponseDto> createAddress(@RequestBody AddressRequestDto requestDto) {
        return ResponseEntity.ok(addressService.createAddress(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDto> getAddressById(@PathVariable ("id") Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/batch")
    public List<AddressResponseDto> getAddressesByIds(@RequestParam ("ids") List<Long> ids) {
        log.info("Received address IDs: {}", ids);
        return addressService.getAllByIds(ids);
    }

    @GetMapping("/search/by-city")
    public List<AddressResponseDto> getAddressesByCity(@RequestParam String city) {
        log.info("📌 Запрос на поиск адресов по городу: {}", city);
        return addressService.getAddressesByCity(city);
    }

    @GetMapping("/search/by-country")
    public List<AddressResponseDto> getAddressesByCountry(@RequestParam String country) {
        log.info("📌 Запрос на поиск адресов по стране: {}", country);
        return addressService.getAddressesByCountry(country);
    }

    @GetMapping("/ids")
    public List<Long> getAddressIdsByCities(@RequestParam List<String> cities) {
        log.info("📌 Получен запрос на поиск адресов по городам: {}", cities);
        List<Long> addressIds = addressService.getAddressIdsByCities(cities);
        log.info("📌 Найденные address IDs: {}", addressIds);
        return addressIds;
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @PathVariable ("id") Long id,
            @RequestBody AddressRequestDto requestDto
    ) {
        return ResponseEntity.ok(addressService.updateAddress(id, requestDto));
    }
}