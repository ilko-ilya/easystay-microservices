package com.samilyak.addressservice.service;

import com.samilyak.addressservice.dto.AddressRequestDto;
import com.samilyak.addressservice.dto.AddressResponseDto;

import java.util.List;

public interface AddressService {

    AddressResponseDto createAddress(AddressRequestDto requestDto);

    AddressResponseDto getAddressById(Long id);

    List<Long> getAddressIdsByCities(List<String> cities);

    AddressResponseDto updateAddress(Long id, AddressRequestDto requestDto);

    List<AddressResponseDto> getAllByIds(List<Long> ids);

    List<AddressResponseDto> getAddressesByCountry(String country);

    List<AddressResponseDto> getAddressesByCity(String city);

}
