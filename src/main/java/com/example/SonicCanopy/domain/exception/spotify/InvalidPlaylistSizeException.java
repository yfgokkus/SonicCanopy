package com.example.SonicCanopy.domain.exception.spotify;

public class InvalidPlaylistSizeException extends RuntimeException {
    public InvalidPlaylistSizeException(String message) {
        super(message);
    }
}
