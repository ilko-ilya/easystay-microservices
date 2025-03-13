package com.samilyak.accommodationservice.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "accommodations")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(255)")
    private Type type;

    @Column(nullable = false, name = "size")
    private String size;

    @ElementCollection
    @CollectionTable(name = "accommodations_amenities",
            joinColumns = @JoinColumn(name = "accommodation_id", referencedColumnName = "id"))
    @Column(nullable = false)
    private List<String> amenities;

    @Column(nullable = false, name = "daily_rate")
    private BigDecimal dailyRate;

    @Column(nullable = false, name = "availability")
    private Integer availability;

    @Column(nullable = false, name = "address_id")
    private Long addressId;

    public enum Type {
        HOUSE,
        APARTMENT,
        CONDO,
        VACATION_HOME
    }
}