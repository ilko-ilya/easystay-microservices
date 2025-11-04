package com.samilyak.accommodationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "availability_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"accommodation_id", "date"}))
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "locked", nullable = false)
    private boolean locked;

}
