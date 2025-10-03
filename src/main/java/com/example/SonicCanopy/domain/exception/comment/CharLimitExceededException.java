package com.example.SonicCanopy.domain.exception.comment;

public class CharLimitExceededException extends RuntimeException {
    public CharLimitExceededException(String message) {
        super(message);
    }
}
