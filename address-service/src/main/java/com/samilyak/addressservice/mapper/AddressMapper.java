package com.samilyak.addressservice.mapper;

import com.samilyak.addressservice.dto.AddressRequestDto;
import com.samilyak.addressservice.dto.AddressResponseDto;
import com.samilyak.addressservice.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequestDto requestDto) {
        return Address.builder()
                .country(requestDto.country())
                .city(requestDto.city())
                .street(requestDto.street())
                .addressLine(requestDto.addressLine())
                .zipCode(requestDto.zipCode())
                .build();
    }

    public AddressResponseDto toDto(Address address) {
        return new AddressResponseDto(
                address.getId(),
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getAddressLine(),
                address.getZipCode()
        );
    }
}
