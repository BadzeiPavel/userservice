package com.innowise.userservice.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CardInfoPatchDto (

  @Size(min = 4, max = 100, message = "Card holder name must be from 4 to 100 characters")
  String holder
) {

}
