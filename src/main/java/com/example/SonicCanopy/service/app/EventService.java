package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.dto.event.CreateEventRequestDto;
import com.example.SonicCanopy.domain.dto.event.EventDto;
import com.example.SonicCanopy.domain.dto.event.UpdateEventRequestDto;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.domain.entity.Club;
import com.example.SonicCanopy.domain.entity.Event;
import com.example.SonicCanopy.domain.entity.Privilege;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.event.EventNotFoundException;
import com.example.SonicCanopy.domain.mapper.EventMapper;
import com.example.SonicCanopy.domain.util.SpotifyContentValidator;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.repository.EventRepository;
import com.example.SonicCanopy.service.infrastructure.spotify.SpotifyContentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public EventDto createEvent(Long clubId, CreateEventRequestDto request, User user) {
        clubAuthorizationService.authorize(clubId, user.getId(), Privilege.MANAGE_EVENTS);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("event cannot be created"));

        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventDurationMs(request.eventDurationMs())
                .club(club)
                .createdBy(user)
                .spotifyContentUri(request.spotifyContentUri())
                .build();

        event = eventRepository.save(event);

        SpotifyContentDto spotifyContentDto = fetchAndValidateSpotifyContent(request.spotifyContentUri()); //returns null if URI is invalid or null

        return eventMapper.toDto(event, spotifyContentDto);
    }

    public void deleteEvent(long clubId, long eventId, User user) {
        clubAuthorizationService.authorize(clubId, user.getId(), Privilege.MANAGE_EVENTS);

        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event " + eventId + " does not belong to club " + clubId
                ));

        eventRepository.delete(event);
    }

    public EventDto updateEvent(Long clubId, Long eventId, UpdateEventRequestDto request, User user) {
        clubAuthorizationService.authorize(clubId, user.getId(),  Privilege.MANAGE_EVENTS);

        Event event = eventRepository.findByIdAndClubId(eventId, clubId)
                .orElseThrow(() -> new EventNotFoundException(
                        "Can't find event " + eventId + " in the club " + clubId
                ));

        // Update mutable fields
        event.setName(request.name());
        event.setDescription(request.description());
        event.setEventDurationMs(request.eventDurationMs());
        event.setSpotifyContentUri(request.spotifyContentUri());

        // Explicit save for clarity
        event = eventRepository.save(event);

        SpotifyContentDto spotifyContentDto = fetchAndValidateSpotifyContent(request.spotifyContentUri());

        return eventMapper.toDto(event, spotifyContentDto);
    }

    public EventDto getEventByIdAndClubId(Long clubId, Long eventId, User user){
        boolean isPrivate = clubRepository.isPrivate(clubId);

        if(isPrivate && !clubAuthorizationService.isMember(clubId, user.getId())){
            throw new UnauthorizedActionException("Club is private");
        }

        Event event = eventRepository.findByIdAndClubId(eventId, clubId).orElseThrow(
                () -> new EventNotFoundException("Event " + eventId + " not found in the club " + clubId)
        );

        SpotifyContentDto spotifyContent = fetchAndValidateSpotifyContent(event.getSpotifyContentUri());

        return eventMapper.toDto(event, spotifyContent);
    }

    public PagedResponse<EventDto> getClubEvents(Long clubId, User user, Pageable pageable, HttpServletRequest request) {
        boolean isPrivate = clubRepository.isPrivate(clubId);

        if(isPrivate && !clubAuthorizationService.isMember(clubId, user.getId())){
            throw new UnauthorizedActionException("Club is private");
        }

        Page<Event> eventPage = eventRepository.findAllByClubIdOrderByCreatedAtDesc(clubId, pageable);
        List<String> uris = eventPage.stream()
                .map(Event::getSpotifyContentUri)
                .filter(Objects::nonNull)
                .toList();

        Map<String, SpotifyContentDto> contentMap = fetchSpotifyContentMappedByUris(uris);

        List<EventDto> eventDtoList = eventPage.stream()
                .map(event -> {
                    String uri = event.getSpotifyContentUri();
                    SpotifyContentDto content = contentMap.get(uri);
                    return eventMapper.toDto(event, content);
                })
                .toList();

        return PagedResponse.of(
                eventDtoList,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages(),
                request
        );
    }

    private Map<String, SpotifyContentDto> fetchSpotifyContentMappedByUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        return uris.parallelStream()
                .map(uri -> {
                    SpotifyContentDto content = fetchAndValidateSpotifyContent(uri);
                    return Map.entry(uri, content);
                })
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private SpotifyContentDto fetchAndValidateSpotifyContent(String uri) {
        SpotifyContentDto content = spotifyContentService.getContent(uri);

        if(!content.isFailed()){
            return SpotifyContentValidator.validate(content, content.getName());
        }

        return content;
    }
}
