package com.example.SonicCanopy.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateClubRequestDto(
        @NotBlank String name,
        @Size(max = 255) String description
) {}
