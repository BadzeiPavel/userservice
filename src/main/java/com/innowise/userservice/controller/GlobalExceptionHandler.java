package com.innowise.userservice.controller;

import com.innowise.userservice.exception.UserServiceException;
import com.innowise.userservice.model.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserServiceException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(UserServiceException ex,
      HttpServletRequest request) {
    return buildResponse("User service exception", ex.getMessage(), ex.getClass().getSimpleName(),
        HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
      HttpServletRequest request) {
    return buildResponse("Bad Request", ex.getMessage(), ex.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return buildResponse("Validation Failed", errors, ex.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    return buildResponse("Internal Server Error", ex.getMessage(), ex.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<ErrorResponse> buildResponse(String title, String message,
      String exceptionName,
      HttpStatus status, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse(
        title,
        exceptionName,
        status.value(),
        message,
        request.getRequestURI(),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(response, status);
  }
}