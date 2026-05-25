package com.innowise.userservice.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    String name,
    String surname,
    LocalDate birthDate,
    String email,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Set<PaymentCardDto> paymentCards
) {

}