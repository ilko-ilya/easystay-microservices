package com.samilyak.addressservice.mapper;

import com.samilyak.addressservice.dto.AddressRequestDto;
import com.samilyak.addressservice.dto.AddressResponseDto;
import com.samilyak.addressservice.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true) // ID создаётся автоматически
    Address toEntity(AddressRequestDto dto);

    AddressResponseDto toDto(Address address);
}