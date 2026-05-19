package com.innowise.userservice.model.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
    String title,
    String name,
    int status,
    String message,
    String path,
    LocalDateTime timestamp
) {

}