package com.example.SonicCanopy.domain.exception.spotify;

public class InvalidSpotifyUriException extends RuntimeException {
  public InvalidSpotifyUriException(String message) {
    super(message);
  }
}
