package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.AuthController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.auth.InvalidCredentialsException;
import com.example.SonicCanopy.domain.exception.auth.InvalidRefreshTokenException;
import com.example.SonicCanopy.domain.exception.auth.RefreshTokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(
        basePackages = "com.example.jwtoken.controller",
        assignableTypes = { AuthController.class }
)
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("Invalid credentials"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("Invalid refresh token"));
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        log.warn("Expired refresh token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("Refresh token expired"));
    }
}

