package com.example.SonicCanopy.exception.handler;

import com.example.SonicCanopy.controller.SpotifyController;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.exception.spotify.InvalidSpotifyUriException;
import com.example.SonicCanopy.exception.spotify.InvalidTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.jwtoken.controller",
        assignableTypes = { SpotifyController.class }
)
public class SpotifyExceptionHandler {

    @ExceptionHandler(InvalidTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoValidTypeFound(InvalidTypeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid type parameter: " + ex.getMessage());
    }
    @ExceptionHandler(InvalidSpotifyUriException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoValidTypeFound(InvalidSpotifyUriException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid uri: " + ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(HttpStatus status, String message) {
        ApiResponse<Void> response = ApiResponse.failure(message, Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase()
        ));
        return ResponseEntity.status(status).body(response);
    }
}
