package com.example.coursebe.common;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private boolean success;
    private String message;
    private Map<String, Object> metadata;
    private T data;

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .success(true)
                .message(message)
                .data(data)
                .build();
    };

    public static <T> ApiResponse<T> success(int code, String message, Map<String, Object> metadata, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .success(true)
                .message(message)
                .metadata(metadata)
                .data(data)
                .build();
    };

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .success(false)
                .message(message)
                .build();
    };
}