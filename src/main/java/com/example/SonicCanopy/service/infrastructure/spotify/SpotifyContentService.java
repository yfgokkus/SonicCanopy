package com.example.SonicCanopy.service.infrastructure.spotify;

import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.domain.mapper.SpotifyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class SpotifyContentService {
    private final SpotifyClient spotifyClient;
    private final SpotifyMapper spotifyMapper;


    public SpotifyContentService(SpotifyClient spotifyClient, SpotifyMapper spotifyMapper) {
        this.spotifyClient = spotifyClient;
        this.spotifyMapper = spotifyMapper;
    }

    public SpotifyContentDto getContent(String id, String type) {
        try {
            return switch (type) {
                case "track" -> spotifyMapper.toTrackDto(spotifyClient.getTrack(id));
                case "album" -> spotifyMapper.toAlbumDto(spotifyClient.getAlbum(id));
                case "artist" -> spotifyMapper.toArtistDto(spotifyClient.getArtist(id));
                case "playlist" -> spotifyMapper.toPlaylistDto(spotifyClient.getPlaylist(id));
                default -> null;
            };
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Spotify {} with ID {} not found", type, id);
            return null; // gracefully handle "not found"
        } catch (WebClientResponseException e) {
            log.error("Spotify API error while fetching {} with ID {}: {}", type, id, e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching {} with ID {} from Spotify", type, id, e);
            return null;
        }
    }


}
