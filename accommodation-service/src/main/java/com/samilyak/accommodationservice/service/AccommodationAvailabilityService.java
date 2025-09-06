package com.samilyak.accommodationservice.service;

import java.time.LocalDate;

public interface AccommodationAvailabilityService {

    void initializeAvailabilitySlots(Long accommodationId, Integer availability);
    void updateAvailabilitySlots(Long accommodationId, Integer newAvailability);
    boolean areDatesAvailable(Long accommodationId, LocalDate checkIn, LocalDate checkOut);
    void lockDates(Long accommodationId, LocalDate checkIn, LocalDate checkOut);
    void unlockDates(Long accommodationId, LocalDate checkIn, LocalDate checkOut);

}