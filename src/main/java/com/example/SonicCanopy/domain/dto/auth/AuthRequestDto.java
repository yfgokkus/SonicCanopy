package com.example.SonicCanopy.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(
        @NotBlank String username,
        @NotBlank String password
) {}
