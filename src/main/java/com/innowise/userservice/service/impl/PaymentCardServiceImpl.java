package com.innowise.userservice.service.impl;

import com.innowise.userservice.config.app.AppProperties;
import com.innowise.userservice.config.cache.RedisConfig;
import com.innowise.userservice.exception.DuplicateCardNumberException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.MaxCardsExceededException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.dto.response.CardsListDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.specification.PaymentCardSpecification;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.util.PaymentCardUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
  @Caching(
      put = @CachePut(value = RedisConfig.CARD_CACHE, key = "#result.id"),
      evict = @CacheEvict(value = RedisConfig.USER_CACHE, key = "#userId")
  )
  public PaymentCardDto createCard(UUID userId, PaymentCardCreationDto dto) {
    User user = userService.getUserEntityById(userId);

    if (cardRepository.countByUserIdAndDeletedFalse(userId) >= appProperties.maxCardsPerUser()) {
      throw new MaxCardsExceededException(
          "User already has the maximum number of cards (" + appProperties.maxCardsPerUser() + ")");
    }
    if (cardRepository.existsByNumberAndDeletedFalse(dto.number())) {
      throw new DuplicateCardNumberException("Card number already exists: " + dto.number());
    }

    PaymentCard card = cardMapper.toPaymentCard(dto);
    card.setUser(user);
    card = cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = RedisConfig.CARD_CACHE, key = "#id")
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
  @Caching(
      put = @CachePut(value = RedisConfig.CARD_CACHE, key = "#id"),
      evict = @CacheEvict(value = RedisConfig.USER_CACHE, key = "#result.userId")
  )
  public PaymentCardDto updateCard(UUID id, PaymentCardPatchDto dto) {
    PaymentCard card = findCardById(id);
    PaymentCardUtil.update(card, dto);
    card = cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  @Caching(
      put = @CachePut(value = RedisConfig.CARD_CACHE, key = "#id"),
      evict = @CacheEvict(value = RedisConfig.USER_CACHE, key = "#result.userId")
  )
  public PaymentCardDto activateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, true);
    if (rows == 0) {
      throw new EntityNotFoundException("Card not found with id: " + id);
    }
    return getCardById(id);
  }

  @Override
  @Caching(
      put = @CachePut(value = RedisConfig.CARD_CACHE, key = "#id"),
      evict = @CacheEvict(value = RedisConfig.USER_CACHE, key = "#result.userId")
  )
  public PaymentCardDto deactivateCard(UUID id) {
    int rows = cardRepository.updateActiveStatus(id, false);
    if (rows == 0) {
      throw new EntityNotFoundException("Card not found with id: " + id);
    }
    return getCardById(id);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = RedisConfig.CARD_CACHE, key = "#id"),
      @CacheEvict(value = RedisConfig.USER_CACHE, key = "#result.userId")
  })
  public PaymentCardDto softDeleteCard(UUID id) {
    PaymentCard card = findCardById(id);
    card.setDeleted(true);
    cardRepository.save(card);
    return cardMapper.toPaymentCardDto(card);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = RedisConfig.CARD_CACHE, key = "#id"),
      @CacheEvict(value = RedisConfig.USER_CACHE, key = "#result.userId")
  })
  public PaymentCardDto hardDeleteCard(UUID id) {
    PaymentCard card = findCardById(id);
    PaymentCardDto deletedDto = cardMapper.toPaymentCardDto(card);
    cardRepository.delete(card);
    return deletedDto;
  }

  private PaymentCard findCardById(UUID id) {
    return cardRepository.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));
  }
}