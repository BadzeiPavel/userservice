package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserCreationDto(
    @NotBlank @Size(min = 1, max = 50)
    String name,

    @NotBlank @Size(min = 1, max = 50)
    String surname,

    @NotNull @Past
    LocalDate birthDate,

    @NotBlank @Email @Size(max = 100)
    String email
) {

}