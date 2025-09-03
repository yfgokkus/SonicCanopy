package com.example.SonicCanopy.domain.exception.club;


public class ClubNotFoundException extends RuntimeException {
    public ClubNotFoundException(String message) {
        super(message);
    }
}