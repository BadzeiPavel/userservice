package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PaymentCardCreationDto(
    @NotBlank @Pattern(regexp = "\\d{16}")
    String number,

    @NotBlank @Size(min = 1, max = 100)
    String holder,

    @NotNull @Future
    LocalDate expirationDate
) {

}