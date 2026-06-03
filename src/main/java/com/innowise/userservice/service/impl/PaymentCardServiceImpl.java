package com.innowise.userservice.service.impl;

import com.innowise.commonstarter.model.dto.PaymentCardDto;
import com.innowise.commonstarter.model.dto.UserDto;
import com.innowise.userservice.config.app.AppProperties;
import com.innowise.userservice.exception.UserServiceException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.dto.response.CardsListDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.repository.PaymentCardRepository;
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
  private final UserMapper userMapper;
  private final EntityManager entityManager;

  @Override
  public PaymentCardDto createCard(UUID userId, PaymentCardCreationDto dto) {
    UserDto user = userService.getUserById(userId);

    if (cardRepository.countByUserIdAndDeletedFalse(userId) >= appProperties.maxCardsPerUser()) {
      throw new UserServiceException(
          "User already has the maximum number of cards (" + appProperties.maxCardsPerUser() + ")");
    }
    if (cardRepository.existsByNumberAndDeletedFalse(dto.number())) {
      throw new UserServiceException("Card number already exists: " + dto.number());
    }

    PaymentCard card = cardMapper.toPaymentCard(dto);
    card.setUser(userMapper.toUser(user));
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
  public CardsListDto getAllCardsByUserId(UUID userId) {
    List<PaymentCardDto> cards = cardRepository.findByUserIdAndDeletedFalse(userId).stream()
        .map(cardMapper::toPaymentCardDto)
        .toList();
    return new CardsListDto(cards);
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
  public PaymentCardDto changeCardActiveStatus(UUID id, boolean active) {
    return active ? activateCard(id) : deactivateCard(id);
  }

  private PaymentCardDto activateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new UserServiceException("Card not found with id: " + id);
    }
    return getCardById(id);
  }

  private PaymentCardDto deactivateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new UserServiceException("Card not found with id: " + id);
    }
    return getCardById(id);
  }

  @Override
  public PaymentCardDto deleteCard(UUID id, boolean hardDeletion) {
    return hardDeletion ? hardDeleteCard(id) : softDeleteCard(id);
  }

  private PaymentCardDto softDeleteCard(UUID id) {
    PaymentCard card = findCardById(id);
    card.setDeleted(true);
    cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  private PaymentCardDto hardDeleteCard(UUID id) {
    PaymentCard card = findCardById(id);
    PaymentCardDto deletedDto = cardMapper.toPaymentCardDto(card);
    cardRepository.delete(card);
    return deletedDto;
  }

  private PaymentCard findCardById(UUID id) {
    return cardRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new UserServiceException("Card not found with id: " + id));
  }
}