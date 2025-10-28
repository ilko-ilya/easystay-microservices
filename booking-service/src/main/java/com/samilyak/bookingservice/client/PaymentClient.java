package com.samilyak.bookingservice.client;

import com.samilyak.bookingservice.dto.client.payment.PaymentRequestDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentResponseDto;
import com.samilyak.bookingservice.dto.client.payment.PaymentStatusDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments")
    PaymentResponseDto createPayment(@RequestBody PaymentRequestDto requestDto);

    @GetMapping("/{bookingId}/status")
    PaymentStatusDto getPaymentStatus(@PathVariable ("bookingId") Long bookingId);

    @DeleteMapping("/payments/{sessionId}")
    void cancelPayment(@PathVariable String sessionId);

}
