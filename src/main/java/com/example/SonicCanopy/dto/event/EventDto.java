package com.example.SonicCanopy.dto.event;

import java.time.LocalDateTime;

public record EventDto(
        Long id,
        String name,
        String description,
        Long eventDurationMs,
        LocalDateTime createdAt,

        Long clubId,
        Long createdById,

        // Spotify content info (nullable)
        String spotifyContentId,
        String spotifyContentType,
        String spotifyContentName,
        Integer spotifyContentLength,
        String spotifyContentArtist,
        String spotifyContentImageUrl,
        String spotifyContentReleaseDate
) {}
