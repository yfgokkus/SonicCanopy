package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.event.CreateEventRequestDto;
import com.example.SonicCanopy.domain.dto.event.EventDto;
import com.example.SonicCanopy.domain.dto.event.UpdateEventRequestDto;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/clubs/{clubId}/events")
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("{eventId}")
    public ResponseEntity<ApiResponse<EventDto>> getEventByIdAndClubId(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user
    ){
        EventDto eventDto = eventService.getEventByIdAndClubId(clubId, eventId, user);

        return ResponseEntity.ok(ApiResponse.success("Event found", eventDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EventDto>>> listEvents(
            @PathVariable Long clubId,
            Pageable pageable,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PagedResponse<EventDto> events = eventService.getClubEvents(clubId, user, pageable, request);
        return ResponseEntity.ok(ApiResponse.success("Club events fetched", events));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDto>> createEvent(
            @PathVariable Long clubId,
            @RequestBody @Valid CreateEventRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        EventDto event = eventService.createEvent(clubId, request, user);

        // Build the Location URI for the new event resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(event.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponse.success("Event created successfully", event));
    }

    @PutMapping("{eventId}")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventRequestDto request,
            @AuthenticationPrincipal User user
    ) {

        EventDto event = eventService.updateEvent(clubId, eventId, request, user);
        return ResponseEntity
                .ok(ApiResponse.success("Event updated successfully", event));
    }

    @DeleteMapping("{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user
    ) {
        eventService.deleteEvent(clubId, eventId, user);
        return ResponseEntity
                .ok(ApiResponse.success("Event deleted successfully"));
    }

}

