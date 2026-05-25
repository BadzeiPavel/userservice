package com.innowise.userservice.service.impl.integration;

import com.innowise.userservice.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCardControllerIT extends BaseControllerIT {

  @Autowired
  private PaymentCardRepository cardRepository;

  @Autowired
  private UserRepository userRepository;

  private UserDto testUser;

  @BeforeEach
  void cleanUp() {
    cardRepository.deleteAll();
    userRepository.deleteAll();

    testUser = restTemplate.postForObject(
        baseUrl() + "/api/v1/users",
        TestDataFactory.createUserCreationDto("CardTest", "User"),
        UserDto.class
    );
  }

  @Test
  void createCard_shouldReturn201() {
    PaymentCardCreationDto dto = TestDataFactory.createCardCreationDto("1111222233334444", "Alice");
    ResponseEntity<PaymentCardDto> response = restTemplate.postForEntity(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(), dto, PaymentCardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().number()).isEqualTo("1111222233334444");
  }

  @Test
  void getCardById_shouldReturn200() {
    PaymentCardDto card = restTemplate.postForObject(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Test"),
        PaymentCardDto.class);

    ResponseEntity<PaymentCardDto> response = restTemplate.getForEntity(
        baseUrl() + "/api/v1/cards/" + card.id(), PaymentCardDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().number()).isEqualTo("1234567890123456");
  }

  @Test
  void getAllCardsByUserId_shouldReturnList() {
    restTemplate.postForObject(baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Holder1"), PaymentCardDto.class);
    restTemplate.postForObject(baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("2234567890123456", "Holder2"), PaymentCardDto.class);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        HttpMethod.GET, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DocumentContext json = JsonPath.parse(response.getBody());
    assertThat(json.read("cards.length()", Integer.class)).isEqualTo(2);
    assertThat(json.read("cards[0].holder", String.class)).isIn("Holder1", "Holder2");
  }

  @Test
  void getCards_shouldReturnPage() {
    restTemplate.postForObject(baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Alice"), PaymentCardDto.class);

    ResponseEntity<String> response = restTemplate.exchange(
        baseUrl() + "/api/v1/cards?holder=alice&page=0&size=10",
        HttpMethod.GET, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DocumentContext json = JsonPath.parse(response.getBody());
    assertThat(json.read("content.length()", Integer.class)).isEqualTo(1);
    assertThat(json.read("content[0].holder", String.class)).isEqualTo("Alice");
  }

  @Test
  void updateCard_shouldReturn200() {
    PaymentCardDto card = restTemplate.postForObject(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Old"),
        PaymentCardDto.class);

    PaymentCardPatchDto patch = new PaymentCardPatchDto(null, "New Holder", null);
    HttpEntity<PaymentCardPatchDto> request = createEntity(patch);
    ResponseEntity<PaymentCardDto> response = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/" + card.id(), HttpMethod.PATCH, request, PaymentCardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().holder()).isEqualTo("New Holder");
  }

  @Test
  void softDeleteCard_shouldReturn204() {
    PaymentCardDto card = restTemplate.postForObject(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Holder"),
        PaymentCardDto.class);

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/" + card.id(),
        HttpMethod.DELETE, null, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<Void> getResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/" + card.id(),
        HttpMethod.GET, null, Void.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void hardDeleteCard_shouldReturn204() {
    PaymentCardDto card = restTemplate.postForObject(
        baseUrl() + "/api/v1/cards/users/" + testUser.id(),
        TestDataFactory.createCardCreationDto("1234567890123456", "Holder"),
        PaymentCardDto.class);

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/" + card.id() + "?hardDeletion=true",
        HttpMethod.DELETE, null, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<Void> getResponse = restTemplate.exchange(
        baseUrl() + "/api/v1/cards/" + card.id(),
        HttpMethod.GET, null, Void.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}