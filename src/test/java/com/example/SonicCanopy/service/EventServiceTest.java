package com.example.SonicCanopy.service;

import com.example.SonicCanopy.domain.dto.event.CreateEventRequestDto;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.domain.mapper.EventMapper;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.repository.EventRepository;
import com.example.SonicCanopy.service.app.ClubAuthorizationService;
import com.example.SonicCanopy.service.app.EventService;
import com.example.SonicCanopy.service.infrastructure.spotify.SpotifyContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private ClubAuthorizationService clubAuthorizationService;
    @Mock private EventMapper eventMapper;
    @Mock private SpotifyContentService spotifyContentService;

    private EventService eventService;
    private User requester;
    private Pageable pageable;

    @BeforeEach
    void setup() {
        eventService = new EventService(
                eventRepository,
                clubAuthorizationService,
                clubRepository,
                eventMapper,
                spotifyContentService
        );

        requester = User.builder()
                .id(1L)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createEvent_should_whenHappyPath() {
        CreateEventRequestDto requestDto = CreateEventRequestDto.builder()
                .name("Listening to Even in Arcadia")
                .description("join the event")
                .eventDurationMs(10L)
                .spotifyContentUri("")
                .build();
    }
}
