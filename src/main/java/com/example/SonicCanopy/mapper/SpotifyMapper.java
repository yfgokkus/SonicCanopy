package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.spotify.*;
import com.example.SonicCanopy.service.spotify.SpotifyClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class SpotifyMapper {

    private final SpotifyClient spotifyClient;

    public SpotifyMapper(SpotifyClient spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    public List<TrackDto> mapTracks(JsonNode response) {
        JsonNode items = response.path("tracks").path("items");
        return StreamSupport.stream(items.spliterator(), false)
                .map(track -> new TrackDto(
                        track.path("id").asText(),
                        track.path("name").asText(),
                        getFirstArtist(track.path("artists")),
                        track.path("album").path("name").asText(),
                        getImageUrl(track.path("album").path("images")),
                        track.path("external_urls").path("spotify").asText(),
                        track.path("duration_ms").asInt(),
                        track.path("album").path("release_date").asText(),
                        track.path("preview_url").isNull() ? null : track.path("preview_url").asText()
                ))
                .toList();
    }

    public PagedResponse<?> mapToPagedResponse(String type, JsonNode result, int offset, int limit, int total) {
        return switch (type) {
            case "track" -> new PagedResponse<>(mapTracks(result), offset, limit, total);
            case "album" -> new PagedResponse<>(mapAlbums(result), offset, limit, total);
            case "artist" -> new PagedResponse<>(mapArtists(result), offset, limit, total);
            case "playlist" -> new PagedResponse<>(mapPlaylists(result), offset, limit, total);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    public List<AlbumDto> mapAlbums(JsonNode searchResponse) {
        JsonNode items = searchResponse.path("albums").path("items");

        return StreamSupport.stream(items.spliterator(), false)
                .map(album -> {
                    String id = album.path("id").asText();

                    long duration = calculateAlbumDuration(id);

                    return new AlbumDto(
                            id,
                            album.path("name").asText(),
                            album.path("album_type").asText(),
                            getAllArtists(album.path("artists")),
                            getImageUrl(album.path("images")),
                            album.path("release_date").asText(),
                            album.path("total_tracks").asInt(),
                            duration,
                            album.path("external_urls").path("spotify").asText()
                    );
                })
                .toList();
    }

    public List<ArtistDto> mapArtists(JsonNode response) {
        JsonNode items = response.path("artists").path("items");
        return StreamSupport.stream(items.spliterator(), false)
                .map(artist -> new ArtistDto(
                        artist.path("id").asText(),
                        artist.path("name").asText(),
                        artist.path("popularity").asInt(),
                        getImageUrl(artist.path("images")),
                        artist.path("external_urls").path("spotify").asText()
                ))
                .toList();
    }

    public List<PlaylistDto> mapPlaylists(JsonNode response) {
        JsonNode items = response.path("playlists").path("items");
        return StreamSupport.stream(items.spliterator(), false)
                .map(playlist -> new PlaylistDto(
                        playlist.path("id").asText(),
                        playlist.path("name").asText(),
                        playlist.path("description").asText(),
                        getImageUrl(playlist.path("images")),
                        playlist.path("external_urls").path("spotify").asText(),
                        playlist.path("owner").path("display_name").asText()
                ))
                .toList();
    }

    private long calculateAlbumDuration(String id) {
        JsonNode detailedAlbum = spotifyClient.getAlbumById(id);
        JsonNode tracks = detailedAlbum.path("tracks").path("items");

        return StreamSupport.stream(tracks.spliterator(), false)
                .mapToLong(track -> track.path("duration_ms").asLong())
                .sum();
    }

    private String getFirstArtist(JsonNode artistsNode) {
        return (artistsNode.isArray() && !artistsNode.isEmpty())
                ? artistsNode.get(0).path("name").asText()
                : "Unknown Artist";
    }

    private List<String> getAllArtists(JsonNode artistsNode) {
        return StreamSupport.stream(artistsNode.spliterator(), false)
                .map(a -> a.path("name").asText())
                .toList();
    }

    private String getImageUrl(JsonNode imagesNode) {
        return (imagesNode.isArray() && !imagesNode.isEmpty())
                ? imagesNode.get(0).path("url").asText()
                : null;
    }
}
