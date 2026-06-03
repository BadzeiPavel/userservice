package com.innowise.userservice.service;

import com.innowise.commonstarter.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.dto.response.CardsListDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

  PaymentCardDto createCard(UUID userId, PaymentCardCreationDto dto);

  PaymentCardDto getCardById(UUID id);

  CardsListDto getAllCardsByUserId(UUID userId);

  Page<PaymentCardDto> getAllCardsWithPagination(Pageable pageable);

  Page<PaymentCardDto> getCardsFiltered(String holder, Pageable pageable);

  PaymentCardDto updateCard(UUID id, PaymentCardPatchDto dto);

  PaymentCardDto changeCardActiveStatus(UUID id, boolean active);

  PaymentCardDto deleteCard(UUID id, boolean hardDeletion);
}