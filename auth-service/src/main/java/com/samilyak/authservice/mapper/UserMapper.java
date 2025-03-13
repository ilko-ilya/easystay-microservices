package com.samilyak.authservice.mapper;

import com.samilyak.authservice.config.MapperConfig;
import com.samilyak.authservice.dto.UserDto;
import com.samilyak.authservice.dto.UserRegistrationRequestDto;
import com.samilyak.authservice.dto.UserRegistrationResponseDto;
import com.samilyak.authservice.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    User toModel(UserRegistrationRequestDto requestDto);

    UserRegistrationResponseDto toDto(User user);

    UserDto toDtoFromModel(User user);
}
