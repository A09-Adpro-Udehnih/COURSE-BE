package com.example.coursebe.exception;

public class UnsupportedSearchTypeException extends RuntimeException {
    public UnsupportedSearchTypeException(String type) {
        super("Unsupported search type: " + type);
    }
}
