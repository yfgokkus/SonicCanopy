package com.example.SonicCanopy.service.infrastructure.spotify;

import com.example.SonicCanopy.domain.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.domain.mapper.SpotifyMapper;
import com.example.SonicCanopy.domain.util.SpotifyUriValidator;
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

    public SpotifyContentDto getContent(String uri) {
        String id = SpotifyUriValidator.extractId(uri);
        String type = SpotifyUriValidator.extractType(uri);

        try {
            return switch (type) {
                case "track" -> spotifyMapper.toTrackDto(spotifyClient.getTrack(id));
                case "album" -> spotifyMapper.toAlbumDto(spotifyClient.getAlbum(id));
                case "artist" -> spotifyMapper.toArtistDto(spotifyClient.getArtist(id));
                case "playlist" -> spotifyMapper.toPlaylistDto(spotifyClient.getPlaylist(id));
                default -> {
                    log.error("Invalid Spotify Content Type: {}", type);
                    yield new SpotifyContentDto(type, uri, "Invalid spotify content type");
                }
            };
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Spotify {} with ID {} not found: {}", type, id, e.getMessage());
            return new SpotifyContentDto(type, uri, "Spotify content with id " + id + "and type " + type + " not found");
        } catch (WebClientResponseException e) {
            log.error("Spotify API error fetching {} with ID {}: {}", type, id, e.getMessage());
            return new SpotifyContentDto(type, uri, "Spotify API error fetching the given uri");
        } catch (RuntimeException e) {
            log.error("Unexpected error fetching {} with ID {}: {}", type, id, e.getMessage());
            return new SpotifyContentDto(type, uri, "Unexpected error fetching the given uri");
        }
    }


}
