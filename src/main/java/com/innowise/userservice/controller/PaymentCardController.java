package com.innowise.userservice.controller;

import com.innowise.common.model.dto.PaymentCardDto;
import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.userservice.model.dto.request.PaymentCardPatchDto;
import com.innowise.userservice.model.dto.response.CardsListDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cards")
public class PaymentCardController {

  private final PaymentCardService cardService;

  @PostMapping("/users/{userId}")
  public ResponseEntity<PaymentCardDto> createCard(
      @PathVariable UUID userId,
      @Valid @RequestBody PaymentCardCreationDto dto) {
    PaymentCardDto created = cardService.createCard(userId, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardDto> getCardById(@PathVariable UUID id) {
    return ResponseEntity.ok(cardService.getCardById(id));
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<CardsListDto> getAllCardsByUserId(@PathVariable UUID userId) {
    return ResponseEntity.ok(cardService.getAllCardsByUserId(userId));
  }

  @GetMapping
  public ResponseEntity<Page<PaymentCardDto>> getCards(
      @RequestParam(required = false) String holder,
      Pageable pageable) {
    return ResponseEntity.ok(cardService.getCardsFiltered(holder, pageable));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<PaymentCardDto> updateCard(
      @PathVariable UUID id,
      @Valid @RequestBody PaymentCardPatchDto dto) {
    return ResponseEntity.ok(cardService.updateCard(id, dto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PaymentCardDto> changeCardActiveStatus(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "true") boolean active) {
    return ResponseEntity.ok(cardService.changeCardActiveStatus(id, active));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCard(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "false") boolean hardDeletion) {
    cardService.deleteCard(id, hardDeletion);
    return ResponseEntity.noContent().build();
  }
}