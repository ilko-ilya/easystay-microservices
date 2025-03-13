package com.samilyak.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/{bookingId}/user-id")
    Long getUserIdByBookingId(@PathVariable ("bookingId") Long bookingId);

}
