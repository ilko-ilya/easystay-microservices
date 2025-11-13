package com.samilyak.accommodationservice.controller;

import com.samilyak.accommodationservice.dto.AccommodationDto;
import com.samilyak.accommodationservice.dto.AccommodationLockCommand;
import com.samilyak.accommodationservice.dto.AccommodationLockResult;
import com.samilyak.accommodationservice.dto.AccommodationRequestDto;
import com.samilyak.accommodationservice.dto.AccommodationUpdateDto;
import com.samilyak.accommodationservice.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@Tag(name = "Accommodation Controller", description = "Endpoints for managing accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping
    @Operation(summary = "Create a new accommodation", description = "Create a new accommodation")
    public ResponseEntity<AccommodationDto> create(
            @RequestBody @Valid AccommodationRequestDto requestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        log.info("🏠 Создание жилья пользователем userId={}, role={}", userId, role);
        return ResponseEntity.status(CREATED).body(accommodationService.create(requestDto));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an accommodation", description = "Only MANAGER can update accommodations")
    public ResponseEntity<AccommodationDto> update(
            @PathVariable("id") Long id,
            @RequestBody @Valid AccommodationUpdateDto updateDto,
            @RequestHeader("X-User-Role") String role
    ) {
        log.info("✏️ Обновление жилья ID={} пользователем с ролью {}", id, role);
        return ResponseEntity.ok(accommodationService.update(id, updateDto));
    }

    @GetMapping
    @Operation(summary = "Get all available accommodations",
            description = "Accessible to any authenticated user")
    public ResponseEntity<List<AccommodationDto>> getAll() {
        log.info("📋 Запрос на получение всех доступных вариантов жилья");
        return ResponseEntity.ok(accommodationService.getAll());
    }

    @GetMapping("/search/by-city")
    public List<AccommodationDto> getAccommodationsByCity(@RequestParam("city") String city) {
        log.info("📌 Запрос на поиск жилья в городе: {}", city);
        return accommodationService.getAccommodationsByCity(city);
    }

    @GetMapping("/search/by-country")
    public List<AccommodationDto> getAccommodationsByCountry(@RequestParam("country") String country) {
        log.info("📌 Запрос на поиск жилья в стране: {}", country);
        return accommodationService.getAccommodationsByCountry(country);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get the accommodation by ID",
            description = "Get the accommodation by ID")
    public ResponseEntity<AccommodationDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(accommodationService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an accommodation by ID",
            description = "Delete an accommodation by ID")
    @ResponseStatus(NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id, @RequestHeader("X-User-Role") String role) {
        log.info("🗑 Удаление жилья ID={} пользователем с ролью {}", id, role);
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only MANAGER can delete accommodations.");
        }
        accommodationService.deleteById(id);
    }

    // ✅ НОВЫЕ ENDPOINTS ДЛЯ РАБОТЫ С ДОСТУПНОСТЬЮ ДАТ

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check accommodation availability",
            description = "Check if accommodation is available for given dates")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("checkIn") LocalDate checkIn,
            @RequestParam("checkOut") LocalDate checkOut
            ) {
        log.info("📅 Проверка доступности жилья {} с {} по {}", id, checkIn, checkOut);
        boolean available = accommodationService.isAvailable(id, checkIn, checkOut);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/{id}/locked-dates")
    @Operation(summary = "Get locked dates for accommodation",
            description = "Returns a list of currently locked dates for the given accommodation")
    public ResponseEntity<List<LocalDate>> getLockedDates(@PathVariable("id") Long id) {
        log.info("📅 Запрос на получение заблокированных дат для жилья {}", id);
        List<LocalDate> lockedDates = accommodationService.getLockedDates(id);
        return ResponseEntity.ok(lockedDates);
    }

    @PostMapping("/{id}/lock-dates")
    @Operation(summary = "Lock accommodation dates",
            description = "Atomically lock accommodation dates for booking")
    public ResponseEntity<AccommodationLockResult> lockDates(
            @PathVariable("id") Long id,
            @RequestBody @Valid AccommodationLockCommand lockCommand,
            @RequestHeader("X-User-Id") String userId) {
        log.info("🔒 Блокировка дат пользователем {} для жилья {}: с {} по {}, версия {}",
                userId, id, lockCommand.checkInDate(), lockCommand.checkOutDate(), lockCommand.expectedVersion());

        AccommodationLockResult result = accommodationService.lockDates(id, lockCommand);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/unlock-dates")
    @Operation(summary = "Unlock accommodation dates",
            description = "Unlock previously locked dates (compensation)")
    public ResponseEntity<Void> unlockDates(
            @PathVariable("id") Long id,
            @RequestBody @Valid AccommodationLockCommand unlockCommand,
            @RequestHeader("X-User-Id") String userId) {
        log.info("🔓 Разблокировка дат пользователем {} для жилья {}: с {} по {}",
                userId, id, unlockCommand.checkInDate(), unlockCommand.checkOutDate());

        accommodationService.unlockDates(id, unlockCommand);
        return ResponseEntity.ok().build();
    }
}
