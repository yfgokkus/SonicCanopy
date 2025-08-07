package com.example.SonicCanopy.dto.spotify;

public record TrackDto(
        String id,
        String name,
        String artist,
        String album,
        String imageUrl,
        String spotifyUrl,
        int durationMs,
        String releaseDate,
        String previewUrl // nullable
) {}