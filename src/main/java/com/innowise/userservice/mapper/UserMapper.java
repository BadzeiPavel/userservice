package com.innowise.userservice.mapper;

import com.innowise.common.model.dto.UserDto;
import com.innowise.common.model.dto.request.UserCreationDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PaymentCardMapper.class})
public interface UserMapper {

  User toUser(UserCreationDto dto);

  @Mapping(target = "paymentCards", ignore = true)
  User toUser(UserDto dto);

  UserDto toUserDto(User entity);
}