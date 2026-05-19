package com.innowise.userservice.exception;

public class MaxCardsExceededException extends RuntimeException {
    public MaxCardsExceededException(String message) { super(message); }
}