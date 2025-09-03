package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.event.EventDto;
import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.domain.entity.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class EventMapper {

    public EventDto toDto(Event event, SpotifyContentDto spotifyContent) {

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