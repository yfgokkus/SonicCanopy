package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.event.EventDto;
import com.example.SonicCanopy.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.entities.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventMapper {

    public EventDto toEventDto(Event event, SpotifyContentDto spotifyContent) {

        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventDurationMs(event.getEventDurationMs())
                .createdAt(LocalDateTime.now())
                .createdById(event.getCreatedBy().getId())
                .spotifyContent(spotifyContent)
                .build();

    }

}