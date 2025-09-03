package com.example.SonicCanopy.domain.exception.clubMember;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String message) {
        super(message);
    }
}