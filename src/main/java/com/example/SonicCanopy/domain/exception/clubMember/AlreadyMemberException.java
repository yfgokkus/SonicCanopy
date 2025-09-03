package com.example.SonicCanopy.domain.exception.clubMember;

public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(String message) {
        super(message);
    }
}
