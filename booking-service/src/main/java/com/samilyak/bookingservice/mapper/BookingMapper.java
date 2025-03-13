package com.samilyak.bookingservice.mapper;

import com.samilyak.bookingservice.dto.booking.BookingRequestDto;
import com.samilyak.bookingservice.dto.booking.BookingResponseDto;
import com.samilyak.bookingservice.model.Booking;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponseDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", expression = "java(userId)")  
    Booking toModel(BookingRequestDto requestDto, @Context Long userId);

}
