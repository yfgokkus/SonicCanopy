package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.spotify.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class SpotifyMapper {

    public PagedSpotifyContent<?> toPagedSpotifyContent(String type, JsonNode result) {
        if (result == null || result.path("items").isEmpty()) {
            throw new IllegalArgumentException("Cannot map to paged content. Item list is either empty or null");
        }

        JsonNode content = result.path(type + "s");
        int offset = content.path("offset").asInt();
        int  limit = content.path("limit").asInt();
        long total = content.path("total").asLong();

        return switch (type) {
            case "track" -> new PagedSpotifyContent<>(toTrackDtoList(result), offset, limit, total);
            case "album" -> new PagedSpotifyContent<>(toAlbumDtoList(result), offset, limit, total);
            case "artist" -> new PagedSpotifyContent<>(toArtistDtoList(result), offset, limit, total);
            case "playlist" -> new PagedSpotifyContent<>(toPlaylistDtoList(result), offset, limit, total);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    public List<TrackDto> toTrackDtoList(JsonNode node) {
        if (node == null) return List.of();

        JsonNode tracks = node.path("tracks").path("items");
        if (!tracks.isArray() || tracks.isEmpty()) {
            return List.of();
        }

        return StreamSupport.stream(tracks.spliterator(), false)
                .map(this::toTrackDto)
                .toList();
    }

    public List<AlbumDto> toAlbumDtoList(JsonNode node) {
        if (node == null) return List.of();

        JsonNode albums =  node.path("albums").path("items");
        if (!albums.isArray() || albums.isEmpty()) {
            return List.of();
        }
        return StreamSupport.stream(albums.spliterator(), false)
                .map(this::toAlbumDto)
                .toList();
    }

    public List<ArtistDto> toArtistDtoList(JsonNode node) {
        if (node == null) return List.of();

        JsonNode artists =  node.path("artists").path("items");
        if (!artists.isArray() || artists.isEmpty()) {
            return List.of();
        }
        return StreamSupport.stream(artists.spliterator(), false)
                .map(this::toArtistDto)
                .toList();
    }

    public List<PlaylistDto> toPlaylistDtoList(JsonNode node) {
        if (node == null) return List.of();

        JsonNode items =  node.path("playlists").path("items");

        if (!items.isArray() || items.isEmpty()) {
            return List.of();
        }

        return StreamSupport.stream(items.spliterator(), false)
                .map(this::toPlaylistDto)
                .toList();
    }

    // MAY REQUIRE extra safety for batch processing or future-proofing against unexpected malformed JSON //
    public TrackDto toTrackDto(JsonNode track) {
        List<String> artists = getArtists(track.path("artists"));
        List<Image> images = getImages(track.path("album").path("images"));
        return TrackDto.builder()
                .name(track.path("name").asText("NaN"))
                .type(track.path("type").asText("NaN"))
                .uri(track.path("uri").asText(null))
                .url(track.path("external_urls").path("spotify").asText(null))
                .images(images)
                .artists(artists)
                .albumType(track.path("album").path("album_type").asText(null))
                .albumName(track.path("album").path("name").asText(null))
                .releaseDate(track.path("album").path("release_date").asText(null))
                .trackNumber(track.path("track_number").asInt())
                .duration(track.path("duration").asLong())
                .build();
    }

    public AlbumDto toAlbumDto(JsonNode album) {
        List<String> artists = getArtists(album.path("artists"));
        List<Image> images = getImages(album.path("images"));
        return AlbumDto.builder()
                .name(album.path("name").asText("NaN"))
                .uri(album.path("uri").asText(null))
                .url(album.path("external_urls").path("spotify").asText(null))
                .images(images)
                .artists(artists)
                .tracks(toTrackDtoList(album))
                .releaseDate(album.path("release_date").asText(null))
                .totalTracks(album.path("total_tracks").asInt())
                .duration(sumOfDurations(album.path("tracks")))
                .build();
    }

    public ArtistDto toArtistDto(JsonNode artist) {
        List<Image> images = getImages(artist.path("images"));
        return ArtistDto.builder()
                .name(artist.path("name").asText("unknown"))
                .uri(artist.path("uri").asText(null))
                .url(artist.path("external_urls").path("spotify").asText(null))
                .images(images)
                .followers(artist.path("followers").path("total").asInt())
                .build();
    }

    public PlaylistDto toPlaylistDto(JsonNode playlist) {
        List<Image> images = getImages(playlist.path("images"));
        return PlaylistDto.builder()
                .name(playlist.path("name").asText("NaN"))
                .uri(playlist.path("uri").asText(null))
                .url(playlist.path("external_urls").path("spotify").asText(null))
                .images(images)
                .ownerName(playlist.path("owner").path("display_name").asText("unknown"))
                .ownerUrl(playlist.path("owner").path("external_urls").path("spotify").asText(null))
                .tracks(toTrackDtoList(playlist))
                .duration(sumOfDurations(playlist.path("tracks")))
                .build();
    }

    private long sumOfDurations(JsonNode tracksNode) {
        if (tracksNode == null
                || !tracksNode.has("items")
                || !tracksNode.path("items").isArray()
                || tracksNode.path("items").isEmpty()) {
            return 0L;
        }

        long totalDuration = 0L;
        for (JsonNode track : tracksNode.path("items")) {
            totalDuration += track.path("duration_ms").asLong(0L);
        }

        return totalDuration;
    }

    private List<String> getArtists(JsonNode artistsNode) {
        return StreamSupport.stream(artistsNode.spliterator(), false)
                .map(a -> a.path("name").asText())
                .toList();
    }

    private List<Image> getImages(JsonNode imagesNode) {
        if (imagesNode == null || !imagesNode.isArray() || imagesNode.isEmpty()) {
            return List.of(); // empty list if missing
        }

        List<Image> images = new ArrayList<>();
        for (JsonNode img : imagesNode) {
            String url = img.path("url").asText(null); // null if missing
            int height = img.path("height").asInt(0);  // 0 if missing
            int width  = img.path("width").asInt(0);   // 0 if missing

            if (url != null) {
                images.add(new Image(url, height, width));
            }
        }

        return images;
    }
}
