package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.PaymentCardPatchDto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

  PaymentCardDto createCard(UUID userId, PaymentCardCreationDto dto);

  PaymentCardDto getCardById(UUID id);

  List<PaymentCardDto> getAllCardsByUserId(UUID userId);

  Page<PaymentCardDto> getAllCardsWithPagination(Pageable pageable);

  Page<PaymentCardDto> getCardsFiltered(String holder, Pageable pageable);

  PaymentCardDto updateCard(UUID id, PaymentCardPatchDto dto);

  void activateCard(UUID id);

  void deactivateCard(UUID id);
}