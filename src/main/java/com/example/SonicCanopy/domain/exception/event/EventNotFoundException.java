package com.example.SonicCanopy.domain.exception.event;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
