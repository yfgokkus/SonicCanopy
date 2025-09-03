package com.example.SonicCanopy.domain.dto.spotify;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrackDto extends SpotifyContentDto{
    private List<String> artists;
    private String albumType; //album or single
    private String albumName; //nullable
    private String releaseDate; //if from album, album release date
    private long duration;
    private int trackNumber;
}