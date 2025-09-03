package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.EventController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.event.EventNotFoundException;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Event not found"));
    }
}
