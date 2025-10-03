package com.example.SonicCanopy.domain.exception.firebase;

public class InvalidImageUrlException extends RuntimeException {

    public InvalidImageUrlException(String message) {
        super(message);
    }

    public InvalidImageUrlException(String message, Object... args) {
        super(org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage());
    }
}