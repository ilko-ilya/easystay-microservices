package com.samilyak.accommodationservice.repository;

import com.samilyak.accommodationservice.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByAccommodationIdAndDateBetween(
            Long accommodationId,
            LocalDate start,
            LocalDate end
    );

    void deleteByAccommodationId(Long accommodationId);

}
