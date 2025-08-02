package com.example.SonicCanopy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> metadata;

    // -------- Success Builders --------
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Request successful")
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Request successful")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Request successful")
                .data(data)
                .metadata(metadata)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }

    // -------- Failure Builders --------

    public static <T> ApiResponse<T> failure(T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Request failed")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message,Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .metadata(metadata)
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }
}
