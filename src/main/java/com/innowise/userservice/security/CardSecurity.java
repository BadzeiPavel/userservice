package com.innowise.userservice.security;

import com.innowise.userservice.service.PaymentCardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cardSecurity")
@RequiredArgsConstructor
public class CardSecurity {

  private final PaymentCardService cardService;

  public boolean isCardOwner(UUID cardId, Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    if (authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      return true;
    }
    UUID currentUserId = UUID.fromString(authentication.getName());
    return cardService.getCardById(cardId)
        .userId().equals(currentUserId);
  }
}