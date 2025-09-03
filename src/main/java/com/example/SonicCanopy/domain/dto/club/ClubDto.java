package com.example.SonicCanopy.domain.dto.club;

import java.time.LocalDateTime;

public record ClubDto(
        Long id,
        String name,
        String description,
        String profilePictureUrl,
        LocalDateTime createdAt,
        String createdByUsername
) {}