package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.event.CreateEventRequestDto;
import com.example.SonicCanopy.dto.event.EventDto;
import com.example.SonicCanopy.dto.event.UpdateEventRequestDto;
import com.example.SonicCanopy.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.Event;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.mapper.EventMapper;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.repository.EventRepository;
import com.example.SonicCanopy.service.spotify.SpotifyContentService;
import com.example.SonicCanopy.utils.SpotifyUriUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final ClubAuthorizationService clubAuthorizationService;
    private final EventMapper eventMapper;
    private final SpotifyContentService spotifyContentService;

    public EventService(EventRepository eventRepository,
                        ClubAuthorizationService clubAuthorizationService,
                        ClubRepository clubRepository,
                        EventMapper eventMapper,
                        SpotifyContentService spotifyContentService) {
        this.eventRepository = eventRepository;
        this.clubAuthorizationService = clubAuthorizationService;
        this.clubRepository = clubRepository;
        this.eventMapper = eventMapper;
        this.spotifyContentService = spotifyContentService;
    }

    @Transactional
    public EventDto createEvent(CreateEventRequestDto request, User requester) {
        clubAuthorizationService.authorizeEventManagement(request.clubId(), requester);

        Club club = clubRepository.findById(request.clubId())
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventDurationMs(request.eventDurationMs())
                .club(club)
                .createdBy(requester)
                .spotifyContentUri(request.spotifyContentUri())
                .build();

        event = eventRepository.save(event);

        SpotifyContentDto spotifyContent = getSpotifyContentValidated(request.spotifyContentUri());

        return eventMapper.toEventDto(event, spotifyContent);
    }

    @Transactional
    public void deleteEvent(long clubId, long eventId, User user) {
        clubAuthorizationService.authorizeEventManagement(clubId, user);

        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event " + eventId + " does not belong to club " + clubId
                ));

        eventRepository.delete(event);
    }

    @Transactional
    public EventDto updateEvent(UpdateEventRequestDto request, User user) {
        clubAuthorizationService.authorizeEventManagement(request.clubId(), user);

        Event event = eventRepository.findByIdAndClubId(request.eventId(), request.clubId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event " + request.eventId() + " does not belong to club " + request.clubId()
                ));

        // Update mutable fields
        event.setName(request.name());
        event.setDescription(request.description());
        event.setEventDurationMs(request.eventDurationMs());
        event.setSpotifyContentUri(request.spotifyContentUri());

        event = eventRepository.save(event);

        SpotifyContentDto spotifyContent = getSpotifyContentValidated(request.spotifyContentUri());

        return eventMapper.toEventDto(event, spotifyContent);
    }

    private SpotifyContentDto getSpotifyContentValidated(String uri){
        if (SpotifyUriUtils.isValid(uri)) {
            try {
                String id = SpotifyUriUtils.extractId(uri);
                String type = SpotifyUriUtils.extractType(uri);
                return spotifyContentService.getContent(id, type);
            } catch (Exception ex) {
                log.warn("Failed to fetch Spotify content error: {}", ex.getMessage());
                return null;
            }
        }
        return null;
    }
}
