package com.samilyak.paymentservice.repository;

import com.samilyak.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(Payment.Status status);

    Collection<Object> findPaymentsByUserId(Long userId);

    Optional<Payment> findBySessionId(String sessionId);
}
