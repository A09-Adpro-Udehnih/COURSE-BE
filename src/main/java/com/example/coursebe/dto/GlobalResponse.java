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
    
    public static <T> GlobalResponse<T> success(HttpStatus code, String message, T data) {
        return GlobalResponse.<T>builder()
                .code(code)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> GlobalResponse<T> success(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }
    
    public static <T> GlobalResponse<T> error(HttpStatus code, String message) {
        return GlobalResponse.<T>builder()
                .code(code)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
    
    public static <T> GlobalResponse<T> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }
    
    public static <T> GlobalResponse<T> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }
}