package com.example.SonicCanopy.domain.exception.user;

public class UserCreationException extends RuntimeException {
    public UserCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
