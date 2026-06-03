package com.innowise.userservice.service.impl.integration;

import com.innowise.userservice.model.dto.request.PaymentCardCreationDto;
import com.innowise.commonstarter.model.dto.request.UserCreationDto;
import java.time.LocalDate;

public class TestDataFactory {

  public static UserCreationDto createUserCreationDto(String name, String surname) {
    return new UserCreationDto(
        name,
        surname,
        LocalDate.now().minusYears(20),
        name.toLowerCase() + "@example.com"
    );
  }

  public static PaymentCardCreationDto createCardCreationDto(String number, String holder) {
    return new PaymentCardCreationDto(
        number,
        holder,
        LocalDate.now().plusYears(3)
    );
  }
}