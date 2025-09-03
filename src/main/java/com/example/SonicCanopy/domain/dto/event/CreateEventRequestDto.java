package com.example.SonicCanopy.domain.dto.event;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CreateEventRequestDto(
        @NotBlank(message = "Name must not be blank")
        String name,

        String description,

        @NotNull(message = "Event duration must be provided")
        @Positive(message = "Event duration must be positive")
        Long eventDurationMs,

        @Positive(message = "Spotify Content ID must be positive")
        String spotifyContentUri
) {}
