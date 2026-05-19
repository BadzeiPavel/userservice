package com.innowise.userservice.model.dto.response;

import com.innowise.userservice.model.dto.PaymentCardDto;
import java.util.List;

public record CardsListDto(
    List<PaymentCardDto> cards
) {

}