package com.example.SonicCanopy.domain.exception.club;

public class UnauthorizedActionException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "You are not allowed to perform this action";

    public UnauthorizedActionException() {
        super(DEFAULT_MESSAGE);
    }

    public UnauthorizedActionException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }
}
