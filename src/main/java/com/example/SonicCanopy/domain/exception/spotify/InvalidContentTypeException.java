package com.example.SonicCanopy.domain.exception.spotify;

public class InvalidContentTypeException extends RuntimeException {
    public InvalidContentTypeException(String message) {
        super(message);
    }
}
