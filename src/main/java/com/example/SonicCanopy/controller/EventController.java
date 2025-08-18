package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.event.CreateEventRequestDto;
import com.example.SonicCanopy.dto.event.EventDto;
import com.example.SonicCanopy.dto.event.UpdateEventRequestDto;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.service.app.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events/{clubId}")
@Slf4j
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDto>> createEvent(
            @PathVariable Long clubId,
            @RequestBody @Valid CreateEventRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        EventDto event = eventService.createEvent(request, user);
        return ResponseEntity.ok(ApiResponse.success("Event created successfully", event));
    }

    @PutMapping("{eventId}")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventRequestDto request,
            @AuthenticationPrincipal User user
    ) {
        // ensure DTO carries the right IDs
        UpdateEventRequestDto updateRequest = new UpdateEventRequestDto(
                eventId,
                clubId,
                request.name(),
                request.description(),
                request.eventDurationMs(),
                request.spotifyContentUri()
        );

        EventDto event = eventService.updateEvent(updateRequest, user);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @DeleteMapping("{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user
    ) {
        eventService.deleteEvent(clubId, eventId, user);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }
}

