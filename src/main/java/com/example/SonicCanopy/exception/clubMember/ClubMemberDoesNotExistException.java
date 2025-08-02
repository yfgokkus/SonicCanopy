package com.example.SonicCanopy.exception.clubMember;

public class ClubMemberDoesNotExistException extends RuntimeException {
    public ClubMemberDoesNotExistException(String message) {
        super(message);
    }
}
