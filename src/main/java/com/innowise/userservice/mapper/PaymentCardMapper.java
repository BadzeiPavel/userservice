package com.innowise.userservice.mapper;

import com.innowise.userservice.model.dto.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.PaymentCardPatchDto;
import com.innowise.userservice.model.entity.PaymentCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentCardMapper {

  @Mapping(target = "user", ignore = true)
  PaymentCard toPaymentCard(PaymentCardCreationDto dto);

  @Mapping(target = "userId", source = "user.id")
  PaymentCardDto toPaymentCardDto(PaymentCard entity);
}