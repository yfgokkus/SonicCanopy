package com.example.SonicCanopy.domain.dto.club;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClubDto(
        Long id,
        String name,
        String description,
        String imageUrl,
        LocalDateTime createdAt
) {}