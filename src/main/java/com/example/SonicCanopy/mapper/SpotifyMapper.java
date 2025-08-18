package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.spotify.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class SpotifyMapper {

    public PagedResponse<?> mapToPagedResponse(String type, JsonNode result, int offset, int limit, int total) {
        return switch (type) {
            case "track" -> new PagedResponse<>(toTrackDtoList(result), offset, limit, total);
            case "album" -> new PagedResponse<>(toAlbumDtoList(result), offset, limit, total);
            case "artist" -> new PagedResponse<>(toArtistDtoList(result), offset, limit, total);
            case "playlist" -> new PagedResponse<>(toPlaylistDtoList(result), offset, limit, total);
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

        JsonNode playlists =  node.path("playlists").path("items");
        if (!playlists.isArray() || playlists.isEmpty()) {
            return List.of();
        }
        return StreamSupport.stream(playlists.spliterator(), false)
                .map(this::toPlaylistDto)
                .toList();
    }

    public TrackDto toTrackDto(JsonNode track) {
        List<String> artists = getArtists(track.path("artists"));
        List<Image> images = getImages(track.path("album").path("images"));
        return TrackDto.builder()
                .name(track.path("name").asText("NaN"))
                .uri(track.path("uri").asText(null))
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
                .images(images)
                .followers(artist.path("followers").path("total").asInt())
                .build();
    }

    public PlaylistDto toPlaylistDto(JsonNode playlist) {
        List<Image> images = getImages(playlist.path("images"));
        return PlaylistDto.builder()
                .name(playlist.path("name").asText("NaN"))
                .uri(playlist.path("uri").asText(null))
                .images(images)
                .ownerName(playlist.path("owner").path("display_name").asText("unknown"))
                .ownerUrl(playlist.path("owner").path("external_urls").path("spotify").asText(null))
                .tracks(toTrackDtoList(playlist))
                .duration(sumOfDurations(playlist.path("tracks")))
                .build();
    }

    //TODO: only sums first page of tracks (items) — if album has >50 tracks, you’d need pagination.
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
