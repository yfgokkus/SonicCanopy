package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.UserController;
import com.example.SonicCanopy.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { UserController.class }
)
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String message) {
        ApiResponse<Void> response = ApiResponse.failure(message, Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase()
        ));
        return ResponseEntity.status(status).body(response);
    }
}
