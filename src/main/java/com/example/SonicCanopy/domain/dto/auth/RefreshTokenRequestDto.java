package com.example.SonicCanopy.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token cannot be blank") String refreshToken
) {}