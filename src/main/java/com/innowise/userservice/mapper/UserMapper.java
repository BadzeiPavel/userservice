package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.UserCreationDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.UserPatchDto;
import com.innowise.userservice.model.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PaymentCardMapper.class})
public interface UserMapper {

  User toUser(UserCreationDto dto);

  UserDto toUserDto(User entity);
}