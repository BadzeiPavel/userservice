package com.innowise.userservice.service.impl.unit;

import com.innowise.userservice.config.app.AppProperties;
import com.innowise.userservice.config.cache.RedisConfig;
import com.innowise.userservice.exception.DuplicateCardNumberException;
import com.innowise.userservice.exception.MaxCardsExceededException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.entity.PaymentCard;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

  @Mock
  private AppProperties appProperties;
  @Mock
  private PaymentCardRepository cardRepository;
  @Mock
  private UserService userService;
  @Mock
  private PaymentCardMapper cardMapper;
  @Mock
  private EntityManager entityManager;
  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private PaymentCardServiceImpl cardService;

  private UUID userId;
  private UUID cardId;
  private User userEntity;
  private PaymentCard cardEntity;
  private PaymentCardDto cardDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    cardId = UUID.randomUUID();
    userEntity = new User();
    userEntity.setId(userId);
    cardEntity = new PaymentCard();
    cardEntity.setId(cardId);
    cardEntity.setUser(userEntity);
    cardDto = new PaymentCardDto(cardId, userId, "1234567890123456", "Holder", null, true, null,
        null);

    lenient().when(appProperties.maxCardsPerUser()).thenReturn(5);

    Cache userCache = mock(Cache.class);
    lenient().when(cacheManager.getCache(RedisConfig.USER_CACHE)).thenReturn(userCache);
    lenient().when(cacheManager.getCache(RedisConfig.CARD_CACHE)).thenReturn(mock(Cache.class));
  }

  @Test
  void createCard_shouldSaveAndReturnDto() {
    PaymentCardCreationDto creationDto = new PaymentCardCreationDto("1234567890123456", "John",
        null);
    when(userService.getUserEntityById(userId)).thenReturn(userEntity);
    when(cardRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(2L);
    when(cardRepository.existsByNumberAndDeletedFalse("1234567890123456")).thenReturn(false);
    when(cardMapper.toPaymentCard(creationDto)).thenReturn(cardEntity);
    when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    PaymentCardDto result = cardService.createCard(userId, creationDto);
    assertThat(result).isEqualTo(cardDto);
    verify(cardRepository).save(cardEntity);
  }

  @Test
  void createCard_shouldThrowWhenMaxExceeded() {
    when(userService.getUserEntityById(userId)).thenReturn(userEntity);
    when(cardRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(5L);
    assertThatThrownBy(() -> cardService.createCard(userId, mock(PaymentCardCreationDto.class)))
        .isInstanceOf(MaxCardsExceededException.class);
  }

  @Test
  void createCard_shouldThrowWhenDuplicateNumber() {
    when(userService.getUserEntityById(userId)).thenReturn(userEntity);
    when(cardRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(1L);
    when(cardRepository.existsByNumberAndDeletedFalse("dup")).thenReturn(true);
    PaymentCardCreationDto dto = new PaymentCardCreationDto("dup", "Holder", null);
    assertThatThrownBy(() -> cardService.createCard(userId, dto))
        .isInstanceOf(DuplicateCardNumberException.class);
  }

  @Test
  void getCardById_shouldReturnCard() {
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);
    PaymentCardDto result = cardService.getCardById(cardId);
    assertThat(result).isEqualTo(cardDto);
  }

  @Test
  void updateCard_shouldApplyPatchAndSave() {
    PaymentCardPatchDto patch = new PaymentCardPatchDto(null, "New Holder", null);
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.updateCard(cardId, patch);
    verify(cardRepository).save(cardEntity);
  }

  @Test
  void activateCard_shouldSetActiveTrue() {
    when(cardRepository.updateActiveStatus(cardId, true)).thenReturn(1);
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.activateCard(cardId);
    verify(cardRepository).updateActiveStatus(cardId, true);
  }

  @Test
  void softDeleteCard_shouldMarkDeleted() {
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.softDeleteCard(cardId);
    assertThat(cardEntity.isDeleted()).isTrue();
  }

  @Test
  void hardDeleteCard_shouldDelete() {
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.hardDeleteCard(cardId);
    verify(cardRepository).delete(cardEntity);
  }
}