package com.samilyak.paymentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "session_url", length = 500)
    private String sessionUrl;

    private String sessionId;

    private String phoneNumber;

    @Column(nullable = false)
    private BigDecimal amountToPay;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    public enum Status {
        PENDING, PAID, CANCELED, FAILED, REFUNDED
    }
}
