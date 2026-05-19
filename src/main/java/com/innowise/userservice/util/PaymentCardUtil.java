package com.innowise.userservice.util;

import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.entity.PaymentCard;

public final class PaymentCardUtil {

  public static void update(PaymentCard card, PaymentCardPatchDto dto) {
    if (dto.number() != null) {
      card.setNumber(dto.number());
    }
    if (dto.holder() != null) {
      card.setHolder(dto.holder());
    }
    if (dto.expirationDate() != null) {
      card.setExpirationDate(dto.expirationDate());
    }
  }
}