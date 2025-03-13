package com.samilyak.addressservice.service;

import com.samilyak.addressservice.dto.AddressRequestDto;
import com.samilyak.addressservice.dto.AddressResponseDto;
import com.samilyak.addressservice.mapper.AddressMapper;
import com.samilyak.addressservice.model.Address;
import com.samilyak.addressservice.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressResponseDto createAddress(AddressRequestDto requestDto) {
        Optional<Address> existingAddress = addressRepository.findByStreetAndAddressLineAndCityAndCountry(
                requestDto.street(),
                requestDto.addressLine(),
                requestDto.city(),
                requestDto.country()
        );

        Address address;
        if (existingAddress.isPresent()) {
            address = existingAddress.get();
        } else {
            address = addressMapper.toEntity(requestDto);
            address = addressRepository.save(address);
        }

        return addressMapper.toDto(address);
    }

    @Override
    public AddressResponseDto getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
        return addressMapper.toDto(address);
    }

    @Override
    public List<Long> getAddressIdsByCities(List<String> cities) {
        return addressRepository.findByCityIn(cities)
                .stream()
                .map(Address::getId)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponseDto updateAddress(Long id, AddressRequestDto requestDto) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));

        address.setCountry(requestDto.country());
        address.setCity(requestDto.city());
        address.setStreet(requestDto.street());
        address.setAddressLine(requestDto.addressLine());
        address.setZipCode(requestDto.zipCode());

        return addressMapper.toDto(address);
    }

    @Override
    public List<AddressResponseDto> getAllByIds(List<Long> ids) {
        return addressRepository.findAllById(ids)
                .stream()
                .map(addressMapper::toDto)
                .toList();
    }

    @Override
    public List<AddressResponseDto> getAddressesByCountry(String country) {
        return addressRepository.findByCountry(country)
                .stream()
                .map(addressMapper::toDto)
                .toList();
    }

    @Override
    public List<AddressResponseDto> getAddressesByCity(String city) {
        return addressRepository.findByCity(city)
                .stream()
                .map(addressMapper::toDto)
                .toList();
    }
}