package com.example.SonicCanopy.dto.spotify;

import java.util.List;

public record AlbumDto(
        String id,
        String name,
        String albumType,
        List<String> artists,
        String imageUrl,
        String releaseDate,
        int totalTracks,
        long albumDuration,
        String spotifyUrl
) {}