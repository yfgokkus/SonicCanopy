package com.example.SonicCanopy.controller.advice;

import com.example.SonicCanopy.controller.SpotifyController;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.exception.spotify.InvalidSpotifyContentException;
import com.example.SonicCanopy.domain.exception.spotify.InvalidSpotifyUriException;
import com.example.SonicCanopy.domain.exception.spotify.InvalidContentTypeException;
import com.example.SonicCanopy.domain.exception.spotify.InvalidPlaylistSizeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(
        basePackages = "com.example.jwtoken.controller",
        assignableTypes = { SpotifyController.class }
)
public class SpotifyExceptionHandler {

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoValidTypeFound(InvalidContentTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Invalid content type: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidSpotifyUriException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoValidTypeFound(InvalidSpotifyUriException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Invalid spotify uri: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidPlaylistSizeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPlaylistSize(InvalidPlaylistSizeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Invalid playlist size: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidSpotifyContentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSpotifyContent(InvalidSpotifyContentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Invalid spotify content: " + ex.getMessage()));
    }
}
