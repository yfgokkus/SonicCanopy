package com.example.SonicCanopy.dto.auth;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public AuthResponseDto(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }
}
