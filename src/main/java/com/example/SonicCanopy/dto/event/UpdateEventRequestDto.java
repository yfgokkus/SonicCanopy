package com.example.SonicCanopy.dto.event;

public record UpdateEventRequestDto(
        Long eventId,
        Long clubId,
        String name,
        String description,
        Long eventDurationMs,
        String spotifyContentUri
) {}
