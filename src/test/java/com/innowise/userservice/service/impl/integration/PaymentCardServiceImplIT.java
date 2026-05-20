package com.innowise.userservice.service.impl.integration;

import com.innowise.userservice.config.app.AppProperties;
import com.innowise.userservice.exception.DuplicateCardNumberException;
import com.innowise.userservice.exception.EntityNotFoundException;
import com.innowise.userservice.exception.MaxCardsExceededException;
import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.dto.response.CardsListDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentCardServiceImplIT extends BaseIntegrationTest {

  @Autowired
  private PaymentCardService cardService;

  @Autowired
  private UserService userService;

  @Autowired
  private PaymentCardRepository cardRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AppProperties appProperties;

  private UserDto testUser;
  private PaymentCardDto testCard;

  @BeforeEach
  void cleanUp() {
    cardRepository.deleteAll();
    userRepository.deleteAll();
    testUser = userService.createUser(TestDataFactory.createUserCreationDto("CardTest", "User"));
    testCard = cardService.createCard(testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Test Holder"));
  }

  @Test
  void createCard_shouldPersistAndReturnDto() {
    PaymentCardCreationDto dto = TestDataFactory.createCardCreationDto("1111222233334444", "Alice");
    PaymentCardDto result = cardService.createCard(testUser.id(), dto);
    assertThat(result.id()).isNotNull();
    assertThat(result.number()).isEqualTo("1111222233334444");
  }

  @Test
  void createCard_shouldThrowWhenMaxCardsExceeded() {
    for (int i = 0; i < appProperties.maxCardsPerUser() - 1; i++) {
      cardService.createCard(testUser.id(),
          TestDataFactory.createCardCreationDto("111122223333444" + i, "Holder"));
    }
    PaymentCardCreationDto extra = TestDataFactory.createCardCreationDto("0000000000000000",
        "Extra");
    assertThatThrownBy(() -> cardService.createCard(testUser.id(), extra))
        .isInstanceOf(MaxCardsExceededException.class);
  }

  @Test
  void createCard_shouldThrowWhenDuplicateNumber() {
    PaymentCardCreationDto dto = TestDataFactory.createCardCreationDto(testCard.number(),
        "Someone");
    assertThatThrownBy(() -> cardService.createCard(testUser.id(), dto))
        .isInstanceOf(DuplicateCardNumberException.class);
  }

  @Test
  void getCardById_shouldReturnCard() {
    PaymentCardDto found = cardService.getCardById(testCard.id());
    assertThat(found.number()).isEqualTo(testCard.number());
  }

  @Test
  void getAllCardsByUserId_shouldReturnList() {
    cardService.createCard(testUser.id(),
        TestDataFactory.createCardCreationDto("9999888877776666", "Holder2"));
    CardsListDto result = cardService.getAllCardsByUserId(testUser.id());
    assertThat(result.cards()).hasSize(2);
  }

  @Test
  void getAllCardsWithPagination_shouldReturnPage() {
    Page<PaymentCardDto> page = cardService.getAllCardsWithPagination(PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  void getCardsFiltered_shouldReturnFilteredByHolder() {
    cardService.createCard(testUser.id(),
        TestDataFactory.createCardCreationDto("1231231231231231", "Alice"));
    Page<PaymentCardDto> page = cardService.getCardsFiltered("alice", PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent().get(0).holder()).isEqualTo("Alice");
  }

  @Test
  void updateCard_shouldChangeFields() {
    PaymentCardPatchDto patch = new PaymentCardPatchDto(null, "New Holder", null);
    PaymentCardDto updated = cardService.updateCard(testCard.id(), patch);
    assertThat(updated.holder()).isEqualTo("New Holder");
  }

  @Test
  void activateDeactivateCard_shouldToggleActive() {
    cardService.deactivateCard(testCard.id());
    PaymentCardDto deactivated = cardService.getCardById(testCard.id());
    assertThat(deactivated.active()).isFalse();

    cardService.activateCard(testCard.id());
    PaymentCardDto activated = cardService.getCardById(testCard.id());
    assertThat(activated.active()).isTrue();
  }

  @Test
  void softDeleteCard_shouldMarkDeleted() {
    cardService.softDeleteCard(testCard.id());
    assertThat(cardRepository.findByIdAndDeletedFalse(testCard.id())).isEmpty();
  }

  @Test
  void hardDeleteCard_shouldRemoveFromDb() {
    UUID cardId = testCard.id();
    cardService.hardDeleteCard(cardId);
    assertThat(cardRepository.findById(cardId)).isEmpty();
  }

  @Test
  void hardDeleteCard_shouldThrowIfNotFound() {
    assertThatThrownBy(() -> cardService.hardDeleteCard(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class);
  }
}