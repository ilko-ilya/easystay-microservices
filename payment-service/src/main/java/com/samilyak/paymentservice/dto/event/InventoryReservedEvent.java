package com.samilyak.paymentservice.dto.event;

import java.math.BigDecimal;

public record InventoryReservedEvent(

        Long bookingId,
        Long userId,
        BigDecimal totalPrice,
        String phoneNumber

) {
}
