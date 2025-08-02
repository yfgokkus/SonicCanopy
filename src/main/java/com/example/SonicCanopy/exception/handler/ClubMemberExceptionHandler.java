package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.ClubController;
import com.example.SonicCanopy.controller.ClubMemberController;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.exception.clubMember.RequestNotFoundException;
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
        return buildResponse(HttpStatus.BAD_REQUEST, "Already a member: " + ex.getMessage());
    }

    @ExceptionHandler(ClubMemberDoesNotExistException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberDoesNotExist(ClubMemberDoesNotExistException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Member does not exist: " + ex.getMessage());
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestNotFound(RequestNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Request could not be found: " + ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String message) {
        ApiResponse<Void> response = ApiResponse.failure(message, Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase()
        ));
        return ResponseEntity.status(status).body(response);
    }
}

