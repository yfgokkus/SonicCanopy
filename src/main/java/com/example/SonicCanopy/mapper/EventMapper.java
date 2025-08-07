package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.event.EventDto;
import com.example.SonicCanopy.entities.Event;

public class EventMapper {

    // SpotifyContentDto is a placeholder class representing the Spotify metadata
    public static EventDto toDto(Event event, SpotifyContentDto spotifyContent) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getEventDurationMs(), // assuming you add this field in Event entity
                event.getCreatedAt(),

                event.getClub().getId(),
                event.getCreatedBy().getId(),

                event.getSpotifyContentId(),
                spotifyContent != null ? spotifyContent.getType() : null,
                spotifyContent != null ? spotifyContent.getName() : null,
                spotifyContent != null ? spotifyContent.getDurationMs() : null,
                spotifyContent != null ? spotifyContent.getArtistName() : null,
                spotifyContent != null ? spotifyContent.getImageUrl() : null,
                spotifyContent != null ? spotifyContent.getReleaseDate() : null
        );
    }
}