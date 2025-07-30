package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.ClubController;
import com.example.SonicCanopy.dto.error.ApiError;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.exception.club.ImageDeletionException;
import com.example.SonicCanopy.exception.club.ImageUploadException;
import com.example.SonicCanopy.exception.club.UnauthorizedActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { ClubController.class}
)
@Slf4j
public class ClubExceptionHandler {

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiError> handleImageUploadException(ImageUploadException ex) {
        ApiError error = new ApiError(400, "Image upload failed", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ImageDeletionException.class)
    public ResponseEntity<ApiError> handleImageDeletionError(ImageDeletionException ex) {
        log.error("Image deletion failed", ex);
        ApiError error = new ApiError(
                500,
                "Image deletion failed",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ApiError(400, "Invalid request", ex.getMessage()));
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<ApiError> handleClubNotFound(ClubNotFoundException ex) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Club not found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedActionException ex) {
        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Unauthorized action",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
