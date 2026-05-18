package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record UserPatchDto (

  @Size(min = 2, max = 50, message = "Name must be from 2 to 50 characters")
  String name,

  @Size(min = 2, max = 50, message = "Surname must be from 2 to 50 characters")
  String surname,

  @Past
  LocalDate birthDate,

  @Email
  @Size(min = 5, max = 100, message = "Email must be from 5 to 100 characters")
  String email
) {

}
