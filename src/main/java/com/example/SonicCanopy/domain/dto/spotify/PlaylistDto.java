package com.example.SonicCanopy.domain.dto.spotify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlaylistDto extends SpotifyContentDto{
    private String ownerName;
    private String ownerUrl;
    private List<TrackDto> tracks;
    private long duration;
}