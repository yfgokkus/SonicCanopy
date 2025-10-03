package com.example.SonicCanopy.domain.exception.firebase;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}
