package com.example.coursebe.exception;

public class ApplicationExistsException extends RuntimeException {
    public ApplicationExistsException(String message) {
        super(message);
    }
}
