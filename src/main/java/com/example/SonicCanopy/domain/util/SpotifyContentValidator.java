package com.example.SonicCanopy.domain.util;

import com.example.SonicCanopy.domain.dto.spotify.PlaylistDto;
import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.domain.exception.spotify.InvalidPlaylistSizeException;
import com.example.SonicCanopy.domain.exception.spotify.InvalidSpotifyContentException;

public class SpotifyContentValidator {
    private static final int MULTI_TRACK_CONTENT_LIMIT = 20;

    private SpotifyContentValidator() {} // prevent instantiation

    public static void validate(SpotifyContentDto content, String type) {
        if(content == null) {
            throw new InvalidSpotifyContentException("Spotify content is null");
        }

        if (type.equals("playlist")) {
            PlaylistDto contentDto = (PlaylistDto) content;
            if (contentDto.getTracks().isEmpty()) {
                throw new InvalidPlaylistSizeException("No tracks found");
            }
            if (contentDto.getTracks().size() > MULTI_TRACK_CONTENT_LIMIT) {
                throw new InvalidPlaylistSizeException("Track limit exceeded");
            }
        }
    }
}
