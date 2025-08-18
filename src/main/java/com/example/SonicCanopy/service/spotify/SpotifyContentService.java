package com.example.SonicCanopy.service.spotify;

import com.example.SonicCanopy.dto.spotify.SpotifyContentDto;
import com.example.SonicCanopy.mapper.SpotifyMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SpotifyContentService {
    private final SpotifyClient spotifyClient;
    private final SpotifyMapper spotifyMapper;


    public SpotifyContentService(SpotifyClient spotifyClient, SpotifyMapper spotifyMapper) {
        this.spotifyClient = spotifyClient;
        this.spotifyMapper = spotifyMapper;
    }

    public SpotifyContentDto getContent(String id, String type){
        return switch (type){
            case "track" -> spotifyMapper.toTrackDto(spotifyClient.getTrack(id));
            case "album" -> spotifyMapper.toAlbumDto(spotifyClient.getAlbum(id));
            case "artist" -> spotifyMapper.toArtistDto(spotifyClient.getArtist(id));
            case "playlist" -> spotifyMapper.toPlaylistDto(spotifyClient.getPlaylist(id));
            default -> null;
        };
    }


}
