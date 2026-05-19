package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PaymentCardMapper.class})
public interface UserMapper {

  User toUser(UserCreationDto dto);

  UserDto toUserDto(User entity);
}