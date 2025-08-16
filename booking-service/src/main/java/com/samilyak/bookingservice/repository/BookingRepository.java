package com.samilyak.bookingservice.repository;

import com.samilyak.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByUserIdAndStatus(Long userId, Booking.Status status);

    List<Booking> findAllByUserId(Long userId);

    @Query("SELECT b FROM Booking b WHERE b.accommodationId = :accommodationId "
            + "AND ((b.checkInDate BETWEEN :checkInDate AND :checkOutDate) OR "
            + "(b.checkOutDate BETWEEN :checkInDate AND :checkOutDate)) "
            + "AND b.status = 'CONFIRMED'")
    List<Booking> findOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("SELECT b FROM Booking b WHERE b.checkOutDate <= :tomorrow AND b.status != 'CANCELED'")
    List<Booking> findExpiredBookings(@Param("tomorrow") LocalDate tomorrow);

    Optional<Booking> findTopByOrderByIdDesc();
}
