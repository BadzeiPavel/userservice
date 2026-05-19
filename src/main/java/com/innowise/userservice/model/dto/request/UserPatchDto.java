package com.innowise.userservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserPatchDto(
    @Size(min = 1, max = 50)
    String name,

    @Size(min = 1, max = 50)
    String surname,

    LocalDate birthDate,

    @Email @Size(max = 100)
    String email
) {

}