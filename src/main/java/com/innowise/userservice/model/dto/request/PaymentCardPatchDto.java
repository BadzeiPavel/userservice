package com.innowise.userservice.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PaymentCardPatchDto(
    @Pattern(regexp = "\\d{16}")
    String number,

    @Size(min = 1, max = 100)
    String holder,

    LocalDate expirationDate
) {

}