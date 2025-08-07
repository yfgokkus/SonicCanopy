package com.example.SonicCanopy.dto.spotify;

public record PlaylistDto(
        String id,
        String name,
        String description,
        String imageUrl,
        String spotifyUrl,
        String owner
) {}