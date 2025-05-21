package com.example.coursebe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponse<T> {
    private HttpStatus code;
    private boolean success;
    private String message;
    private T data;
} 