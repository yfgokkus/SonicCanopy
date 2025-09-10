package com.example.SonicCanopy.domain.util;

import com.example.SonicCanopy.domain.dto.spotify.PlaylistDto;
import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;

public class SpotifyContentValidator {
    private static final int MULTI_TRACK_CONTENT_LIMIT = 20;

    private SpotifyContentValidator() {}

    public static SpotifyContentDto validate(SpotifyContentDto content, String type) {
        if (type.equals("playlist") && content instanceof PlaylistDto contentDto) {
            if (contentDto.getTracks().isEmpty()) {
                return new SpotifyContentDto(type, content.getUri(), "No tracks found");
            }
            if (contentDto.getTracks().size() > MULTI_TRACK_CONTENT_LIMIT) {
                return new SpotifyContentDto(type, content.getUri(), "Invalid playlists size");
            }
        }
        return  content;
    }
}
