package com.example.SonicCanopy.dto.spotify;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AlbumDto extends SpotifyContentDto{
    private List<String> artists;
    private List<TrackDto> tracks;
    private String releaseDate;
    private int totalTracks;
    private long duration; // derived
}
