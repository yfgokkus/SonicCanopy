package com.example.SonicCanopy.dto.spotify;

public record ArtistDto(
        String id,
        String name,
        int popularity,
        String imageUrl,
        String spotifyUrl
) {}