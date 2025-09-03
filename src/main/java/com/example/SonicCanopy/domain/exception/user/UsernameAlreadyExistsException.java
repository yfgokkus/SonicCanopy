package com.example.SonicCanopy.domain.exception.user;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' is already taken.");
    }
}
