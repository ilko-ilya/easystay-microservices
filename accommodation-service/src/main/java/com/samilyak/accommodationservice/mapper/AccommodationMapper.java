package com.samilyak.accommodationservice.mapper;

import com.samilyak.accommodationservice.dto.AccommodationDto;
import com.samilyak.accommodationservice.dto.AccommodationRequestDto;
import com.samilyak.accommodationservice.dto.AddressResponseDto;
import com.samilyak.accommodationservice.model.Accommodation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addressId", ignore = true)
    Accommodation toModel(AccommodationRequestDto dto);

    @Mapping(target = "location", source = "addressId", qualifiedByName = "mapAddressIdToLocation")
    AccommodationDto toDto(Accommodation accommodation);

    @Named("mapAddressIdToLocation")
    default AddressResponseDto mapAddressIdToLocation(Long addressId) {
        if (addressId == null) {
            return null;
        }
        return new AddressResponseDto(addressId, null, null, null, null, null);
    }
}

