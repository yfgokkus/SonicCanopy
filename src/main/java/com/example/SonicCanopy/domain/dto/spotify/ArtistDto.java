package com.example.SonicCanopy.domain.dto.spotify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArtistDto extends SpotifyContentDto{
    private int followers;
}