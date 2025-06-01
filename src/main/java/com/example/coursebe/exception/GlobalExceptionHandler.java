package com.example.coursebe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import com.example.coursebe.dto.GlobalResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        GlobalResponse<Map<String, String>> response = GlobalResponse.<Map<String, String>>builder()
                .code(HttpStatus.BAD_REQUEST)
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        GlobalResponse<Void> response = GlobalResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST)
                .success(false)
                .message("Invalid input: " + ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApplicationExistsException.class)
    public ResponseEntity<GlobalResponse<Void>> handleApplicationExistsException(ApplicationExistsException ex) {
        GlobalResponse<Void> response = GlobalResponse.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<GlobalResponse<Void>> handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        GlobalResponse<Void> response = GlobalResponse.notFound(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TutorNotAcceptedException.class)
    public ResponseEntity<GlobalResponse<Void>> handleTutorNotAccepted(TutorNotAcceptedException ex) {
        GlobalResponse<Void> response = GlobalResponse.<Void>builder()
                .code(HttpStatus.FORBIDDEN)
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(CourseOwnershipException.class)
    public ResponseEntity<GlobalResponse<Void>> handleCourseOwnership(CourseOwnershipException ex) {
        GlobalResponse<Void> response = GlobalResponse.<Void>builder()
                .code(HttpStatus.FORBIDDEN)
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<GlobalResponse<Void>> handleInvalidUser(InvalidUserException ex) {
        GlobalResponse<Void> response = GlobalResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST)
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalResponse<Void>> handleRuntimeException(RuntimeException ex) {
        GlobalResponse<Void> response = GlobalResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST)
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}