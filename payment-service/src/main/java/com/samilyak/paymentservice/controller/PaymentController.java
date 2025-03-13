package com.samilyak.paymentservice.controller;

import com.samilyak.paymentservice.dto.PaymentRequestDto;
import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.model.Payment;
import com.samilyak.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody PaymentRequestDto request,
            @RequestHeader(name = AUTHORIZATION, required = false) String authHeader) {
        log.info("💳 Создание платежа для бронирования: {}", request.bookingId());
        log.info("🚀 PaymentController получил заголовок Authorization: {}", authHeader);
        log.info("📌 Получен phoneNumber в PaymentController: {}", request.phoneNumber());
        PaymentResponseDto payment = paymentService.createPayment(request);
        return ResponseEntity.status(CREATED).body(payment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable UUID paymentId) {
        log.info("📌 Получение платежа по ID: {}", paymentId);
        PaymentResponseDto payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable UUID paymentId,
                                                    @RequestParam Payment.Status status) {
        log.info("🔄 Обновление статуса платежа {} на {}", paymentId, status);
        paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(@PathVariable Long userId) {
        log.info("📊 Получение всех платежей пользователя: {}", userId);
        List<PaymentResponseDto> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
}
