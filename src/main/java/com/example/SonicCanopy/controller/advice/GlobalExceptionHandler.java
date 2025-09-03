package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.ClubController;
import com.example.SonicCanopy.controller.ClubMemberController;
import com.example.SonicCanopy.controller.EventController;
import com.example.SonicCanopy.controller.UserController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { ClubController.class, ClubMemberController.class, UserController.class, EventController.class}
)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Illegal argument: " + ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure("Unauthorized action: " + ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Database constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure("A database constraint was violated"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        (existing, replacement) -> existing
                ));

        String message = "Validation failed for " + errors.size() + " field(s)";

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(message, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOtherExceptions(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("An unexpected error occurred"));
    }

}

