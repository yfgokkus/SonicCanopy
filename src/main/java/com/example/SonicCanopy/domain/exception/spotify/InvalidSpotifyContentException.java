package com.example.SonicCanopy.domain.exception.spotify;

public class InvalidSpotifyContentException extends RuntimeException {
    public InvalidSpotifyContentException(String message) {
        super(message);
    }
}
