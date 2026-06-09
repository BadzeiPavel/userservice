package com.innowise.userservice.service.impl.unit;

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
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.specification.SpecificationHelper;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.service.impl.PaymentCardServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
  private UserMapper userMapper;

  @InjectMocks
  private PaymentCardServiceImpl cardService;

  private UUID userId;
  private UUID cardId;
  private UserDto userDto;
  private PaymentCard cardEntity;
  private PaymentCardDto cardDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    cardId = UUID.randomUUID();
    User userEntity = new User();
    userEntity.setId(userId);
    userDto = UserDto.builder()
        .id(userId)
        .build();
    cardEntity = new PaymentCard();
    cardEntity.setId(cardId);
    cardEntity.setUser(userEntity);
    cardDto = new PaymentCardDto(cardId, userId, "1234567890123456", "Holder", null, true, null,
        null);

    lenient().when(appProperties.maxCardsPerUser()).thenReturn(5);
  }

  @Test
  void createCard_shouldSaveAndReturnDto() {
    PaymentCardCreationDto creationDto = new PaymentCardCreationDto("1234567890123456", "John",
        null);
    when(userService.getUserById(userId)).thenReturn(userDto);
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
    when(userService.getUserById(userId)).thenReturn(userDto);
    when(cardRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(5L);
    assertThatThrownBy(() -> cardService.createCard(userId, mock(PaymentCardCreationDto.class)))
        .isInstanceOf(UserServiceException.class);
  }

  @Test
  void createCard_shouldThrowWhenDuplicateNumber() {
    when(userService.getUserById(userId)).thenReturn(userDto);
    when(cardRepository.countByUserIdAndDeletedFalse(userId)).thenReturn(1L);
    when(cardRepository.existsByNumberAndDeletedFalse("dup")).thenReturn(true);
    PaymentCardCreationDto dto = new PaymentCardCreationDto("dup", "Holder", null);
    assertThatThrownBy(() -> cardService.createCard(userId, dto))
        .isInstanceOf(UserServiceException.class);
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

    cardService.changeCardActiveStatus(cardId, true);
    verify(cardRepository).updateActiveStatus(cardId, true);
  }

  @Test
  void getAllCardsByUserId_shouldReturnCardsList() {
    List<PaymentCard> cards = List.of(cardEntity);
    when(cardRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(cards);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    CardsListDto result = cardService.getAllCardsByUserId(userId);
    assertThat(result.cards()).containsExactly(cardDto);
  }

  @Test
  void getAllCardsWithPagination_shouldReturnPage() {
    Page<PaymentCard> page = new PageImpl<>(List.of(cardEntity));
    when(cardRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(page);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    Page<PaymentCardDto> result = cardService.getAllCardsWithPagination(PageRequest.of(0, 10));
    assertThat(result.getContent()).containsExactly(cardDto);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void getCardsFiltered_shouldReturnFilteredPage() {
    try (MockedStatic<SpecificationHelper> mockedHelper = mockStatic(SpecificationHelper.class)) {
      Page<PaymentCard> page = new PageImpl<>(List.of(cardEntity));
      mockedHelper.when(() -> SpecificationHelper.findPage(
              any(), any(), any(), any()))
          .thenReturn(page);
      when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

      Page<PaymentCardDto> result = cardService.getCardsFiltered("holder", PageRequest.of(0, 10));
      assertThat(result.getContent()).containsExactly(cardDto);
    }
  }

  @Test
  void softDeleteCard_shouldMarkDeleted() {
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardRepository.save(cardEntity)).thenReturn(cardEntity);
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.deleteCard(cardId, false);
    assertThat(cardEntity.isDeleted()).isTrue();
  }

  @Test
  void hardDeleteCard_shouldDelete() {
    when(cardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(cardEntity));
    when(cardMapper.toPaymentCardDto(cardEntity)).thenReturn(cardDto);

    cardService.deleteCard(cardId, true);
    verify(cardRepository).delete(cardEntity);
  }
}