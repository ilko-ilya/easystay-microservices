package com.samilyak.paymentservice.mapper;

import com.samilyak.paymentservice.dto.PaymentRequestDto;
import com.samilyak.paymentservice.dto.PaymentResponseDto;
import com.samilyak.paymentservice.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toModel(PaymentRequestDto requestDto);

    PaymentResponseDto toDto(Payment payment);
}
