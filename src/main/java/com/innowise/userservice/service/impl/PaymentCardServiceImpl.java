package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.PaymentCardPatchDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.properties.AppProperties;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.repository.specification.PaymentCardSpecification;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCardServiceImpl implements PaymentCardService {

  private final AppProperties appProperties;
  private final PaymentCardRepository cardRepository;
  private final UserService userService;
  private final PaymentCardMapper cardMapper;
  private final EntityManager entityManager;

  @Override
  public PaymentCardDto createCard(UUID userId, PaymentCardCreationDto dto) {
    User user = userService.getUserEntityById(userId);

    if (cardRepository.countByUserIdAndDeletedFalse(userId) >= appProperties.maxCardsPerUser()) {
      throw new IllegalStateException(
          "User already has the maximum number of cards (" + appProperties.maxCardsPerUser() + ")");
    }
    if (cardRepository.existsByNumberAndDeletedFalse(dto.number())) {
      throw new IllegalArgumentException("Card number already exists: " + dto.number());
    }

    PaymentCard card = cardMapper.toPaymentCard(dto);
    card.setUser(user);
    card = cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentCardDto getCardById(UUID id) {
    PaymentCard card = findCardById(id);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentCardDto> getAllCardsByUserId(UUID userId) {
    return cardRepository.findByUserIdAndDeletedFalse(userId).stream()
        .map(cardMapper::toPaymentCardDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentCardDto> getAllCardsWithPagination(Pageable pageable) {
    return cardRepository.findByDeletedFalse(pageable)
        .map(cardMapper::toPaymentCardDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentCardDto> getCardsFiltered(String holder, Pageable pageable) {
    Specification<PaymentCard> spec = PaymentCardSpecification.withFilters(holder);
    Page<PaymentCard> page = SpecificationHelper.findPage(entityManager, PaymentCard.class, spec,
        pageable);
    return page.map(cardMapper::toPaymentCardDto);
  }

  @Override
  public PaymentCardDto updateCard(UUID id, PaymentCardPatchDto dto) {
    PaymentCard card = findCardById(id);
    if (dto.number() != null) {
      card.setNumber(dto.number());
    }
    if (dto.holder() != null) {
      card.setHolder(dto.holder());
    }
    if (dto.expirationDate() != null) {
      card.setExpirationDate(dto.expirationDate());
    }
    card = cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  public void activateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new EntityNotFoundException("Card not found with id: " + id);
    }
  }

  @Override
  public void deactivateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new EntityNotFoundException("Card not found with id: " + id);
    }
  }

  private PaymentCard findCardById(UUID id) {
    return cardRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
  }
}