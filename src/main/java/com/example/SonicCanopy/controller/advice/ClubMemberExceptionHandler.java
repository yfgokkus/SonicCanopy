package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.ClubController;
import com.example.SonicCanopy.controller.ClubMemberController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.domain.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.domain.exception.clubMember.RequestNotFoundException;
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
public class ClubMemberExceptionHandler {

    @ExceptionHandler(AlreadyMemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyMember(AlreadyMemberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Already a member: " + ex.getMessage()));
    }

    @ExceptionHandler(ClubMemberDoesNotExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberDoesNotExist(ClubMemberDoesNotExistException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Member does not exist: " + ex.getMessage()));
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestNotFound(RequestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Request could not be found: "+ ex.getMessage()));
    }
}

