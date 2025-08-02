package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.AuthController;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.exception.auth.InvalidCredentialsException;
import com.example.SonicCanopy.exception.auth.InvalidRefreshTokenException;
import com.example.SonicCanopy.exception.auth.RefreshTokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.jwtoken.controller",
        assignableTypes = { AuthController.class }
)
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        log.warn("Expired refresh token: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String message) {
        ApiResponse<Void> response = ApiResponse.failure(message, Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase()
        ));
        return ResponseEntity.status(status).body(response);
    }
}

