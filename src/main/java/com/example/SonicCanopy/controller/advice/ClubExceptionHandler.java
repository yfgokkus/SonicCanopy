package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.ClubController;
import com.example.SonicCanopy.controller.ClubMemberController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.exception.club.ImageDeletionException;
import com.example.SonicCanopy.domain.exception.club.ImageUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { ClubController.class, ClubMemberController.class }
)
@Slf4j
public class ClubExceptionHandler {

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageUploadException(ImageUploadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Image upload failed: " + ex.getMessage()));
    }

    @ExceptionHandler(ImageDeletionException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageDeletionError(ImageDeletionException ex) {
        log.error("Image deletion failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("Image deletion failed: " + ex.getMessage()));
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleClubNotFound(ClubNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Club not found: " + ex.getMessage()));
    }
}
