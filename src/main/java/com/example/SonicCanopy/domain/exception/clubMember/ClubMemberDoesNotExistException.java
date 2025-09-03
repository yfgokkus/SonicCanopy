package com.example.SonicCanopy.domain.exception.clubMember;

public class ClubMemberDoesNotExistException extends RuntimeException {
    public ClubMemberDoesNotExistException(String message) {
        super(message);
    }
}
