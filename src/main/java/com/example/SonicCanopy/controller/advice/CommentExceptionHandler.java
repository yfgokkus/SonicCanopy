package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.CommentController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.comment.CommentLikeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(
        basePackages = "com.example.SonicCanopy.controller",
        assignableTypes = { CommentController.class}
)
public class CommentExceptionHandler {
    @ExceptionHandler(CommentLikeException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyLiked(CommentLikeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
    }
}
