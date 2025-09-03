package com.example.SonicCanopy.domain.dto.event;

import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        Long id,
        String name,
        String description,
        Long eventDurationMs,
        LocalDateTime createdAt,

        Long clubId,
        Long createdById,

        // Spotify content info (nullable)
        SpotifyContentDto spotifyContent
) {}
