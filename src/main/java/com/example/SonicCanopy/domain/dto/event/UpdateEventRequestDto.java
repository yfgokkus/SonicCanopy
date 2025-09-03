package com.example.SonicCanopy.domain.dto.event;

public record UpdateEventRequestDto(
        String name,
        String description,
        Long eventDurationMs,
        String spotifyContentUri
) {}
