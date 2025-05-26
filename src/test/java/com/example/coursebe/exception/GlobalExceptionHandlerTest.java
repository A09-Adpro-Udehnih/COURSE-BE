package com.example.coursebe.exception;

import com.example.coursebe.dto.GlobalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "default message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<GlobalResponse<Map<String, String>>> response = 
            exceptionHandler.handleValidationExceptions(ex);

        GlobalResponse<Map<String, String>> body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response);
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Validation failed", body.getMessage());
        assertNotNull(body.getData());
        assertEquals("default message", body.getData().get("field"));
    }

    @Test
    void handleRuntimeException() {
        String errorMessage = "Something went wrong";
        RuntimeException ex = new RuntimeException(errorMessage);

        ResponseEntity<GlobalResponse<Void>> response = 
            exceptionHandler.handleRuntimeException(ex);

        GlobalResponse<Void> body = response.getBody();
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response);
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }
} 