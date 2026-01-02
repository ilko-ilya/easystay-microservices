package com.samilyak.bookingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
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
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Ссылка на пользователя в auth-service

    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId; // Ссылка на жильё в accommodation-service

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "payment_id", unique = true)
    private String paymentId;

    @Column(name = "dates_unlocked", nullable = false)
    private boolean datesUnlocked = false;

    @Column(name = "payment_canceled", nullable = false)
    private boolean paymentCanceled = false;

    /**
     * Do we expect payment refund during cancellation?
     * true  -> confirmed booking, money was taken
     * false -> failed booking, no money involved
     */
    @Column(name = "refund_needed", nullable = false)
    private boolean refundNeeded = false;

    /**
     * Booking successfully created: payment ok, dates locked
     */
    public void confirm() {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Only PENDING booking can be confirmed");
        }
        this.status = Status.CONFIRMED;
        this.refundNeeded = true;
    }

    public void startCancellation() {
        if (status != Status.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED booking can be canceled");
        }
        this.status = Status.CANCELING;
        // refundNeeded already true
    }

    public void failBooking() {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Only PENDING booking can be failed");
        }
        this.status = Status.CANCELING;
        this.refundNeeded = false;
    }

    public void markPaymentCanceled() {

        if (paymentCanceled) {
            return;
        }

        if (status == Status.CANCELED || status == Status.EXPIRED) {
            return;
        }

        if (status != Status.CANCELING) {
            throw new IllegalStateException(
                    "Payment can be canceled only during CANCELING. Current status: " + status
            );
        }

        this.paymentCanceled = true;
        tryFinishCancellation();
    }

    public void markDatesUnlocked() {

        if (datesUnlocked) {
            return;
        }

        if (status == Status.CANCELED || status == Status.EXPIRED) {
            return;
        }

        if (status != Status.CANCELING) {
            throw new IllegalStateException(
                    "Dates can be unlocked only during CANCELING. Current status: " + status
            );
        }

        this.datesUnlocked = true;
        tryFinishCancellation();
    }

    private void tryFinishCancellation() {

        if (refundNeeded) {
            // CONFIRMED → CANCELING → CANCELED
            if (paymentCanceled && datesUnlocked) {
                this.status = Status.CANCELED;
            }
        } else {
            // PENDING → CANCELING → EXPIRED
            if (datesUnlocked) {
                this.status = Status.EXPIRED;
            }
        }
    }


    public enum Status {
        PENDING, CONFIRMED, CANCELED, CANCELING, CANCEL_FAILED, EXPIRED
    }

}
