package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.EventController;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.exception.event.EventNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { EventController.class}
)
@Slf4j
public class EventExceptionHandler {
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEventNotFound(EventNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Event not found: " + ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String message) {
        ApiResponse<Void> response = ApiResponse.failure(message, Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase()
        ));
        return ResponseEntity.status(status).body(response);
    }
}
