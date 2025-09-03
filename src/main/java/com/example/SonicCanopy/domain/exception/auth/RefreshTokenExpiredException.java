package com.example.SonicCanopy.domain.exception.auth;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
