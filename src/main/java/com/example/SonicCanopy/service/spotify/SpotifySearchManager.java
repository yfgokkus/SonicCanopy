package com.example.SonicCanopy.service.spotify;

import com.example.SonicCanopy.dto.spotify.PagedResponse;
import com.example.SonicCanopy.mapper.SpotifyMapper;
import com.example.SonicCanopy.dto.spotify.MultiTypeContentDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class SpotifySearchManager {

    private final SpotifyClient spotifyClient;
    private final SpotifyMapper spotifyMapper;
    private final ObjectMapper objectMapper;
    private final Set<String> CONTENT_TYPES = Set.of("track", "album", "artist", "playlist");

    private static final int SHALLOW_SEARCH_LIMIT = 5;

    public SpotifySearchManager(SpotifyClient spotifyClient, SpotifyMapper spotifyMapper, ObjectMapper objectMapper) {
        this.spotifyClient = spotifyClient;
        this.spotifyMapper = spotifyMapper;
        this.objectMapper = objectMapper;
    }

    public Object search(String q, Set<String> typeSet, int offset, int limit) {
        //type list is never null
        List<String> validatedTypes = validateTypesAsList(typeSet);

        // empty or all types
        if (validatedTypes.isEmpty() || new HashSet<>(validatedTypes).containsAll(CONTENT_TYPES)) {
            return shallowSearch(q, validatedTypes);
        }

        // only playlists
        if (validatedTypes.size() == 1 && "playlist".equals(validatedTypes.getFirst())) {
            JsonNode playlistNode = compensatedPlaylistSearch(q, offset, limit);
            return spotifyMapper.toPlaylistDtoList(playlistNode);
        }

        // single type but not playlist
        if (validatedTypes.size() == 1) {
            return singleTypePagedSearch(q, validatedTypes, offset, limit);
        }

        return shallowSearch(q, validatedTypes);
    }

    private PagedResponse<?> singleTypePagedSearch(String q, List<String> types , int offset, int limit) {
        String queryTypes = String.join(",", types);
        JsonNode result = spotifyClient.search(q, queryTypes, offset, limit);

        String singleType = types.getFirst();
        int total = result.path(singleType + "s").path("total").asInt();

        return spotifyMapper.mapToPagedResponse(singleType, result, offset, limit, total);
    }

    private MultiTypeContentDto shallowSearch(String q, List<String> typesParam) { // types is either empty, or includes all types, or multiple without playlists

        List<String> types = typesParam.isEmpty() ? new ArrayList<>(CONTENT_TYPES) : typesParam;
        JsonNode playlistNode = null;
        JsonNode others;

        if(!types.contains("playlist")) {
            String queryTypes = String.join(",", types);
            others = spotifyClient.search(q, queryTypes, 0, SHALLOW_SEARCH_LIMIT);

        }else{
            List<String> nonPlaylistTypes = types.stream()
                    .filter(t -> !"playlist".equals(t))
                    .toList();

            CompletableFuture<JsonNode> playlistsFuture = CompletableFuture.supplyAsync(
                    () -> compensatedPlaylistSearch(q, 0, SHALLOW_SEARCH_LIMIT)
            );

            CompletableFuture<JsonNode> othersFuture = CompletableFuture.supplyAsync(
                    () -> {
                        if (nonPlaylistTypes.isEmpty()) {
                            return objectMapper.createObjectNode();
                        }
                        String queryTypes = String.join(",", nonPlaylistTypes);
                        return spotifyClient.search(q, queryTypes, 0, SHALLOW_SEARCH_LIMIT);
                    }
            );

            try {
                playlistNode = playlistsFuture.get();
                others = othersFuture.get();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Async multi-type playlist search interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Async multi-type playlist search failed", e.getCause());
            }
        }

        return new MultiTypeContentDto(
                spotifyMapper.toTrackDtoList(others),
                spotifyMapper.toAlbumDtoList(others),
                spotifyMapper.toArtistDtoList(others),
                spotifyMapper.toPlaylistDtoList(playlistNode)
        );
    }

    private JsonNode compensatedPlaylistSearch(String query, int offset, int desiredLimit) {
        int batchSize = desiredLimit + 5;
        int total = 0;
        int scanned = 0;

        ArrayNode validItems = objectMapper.createArrayNode();

        while (validItems.size() < desiredLimit) {
            JsonNode response = spotifyClient.search(query, "playlist", offset + scanned, batchSize);
            JsonNode playlistNode = response.path("playlists");
            JsonNode playlists = playlistNode.path("items");

            if (total == 0) {
                total = playlistNode.path("total").asInt();
            }

            if (playlists.isEmpty()) {
                break;
            }

            for (int i = 0; i < playlists.size(); i++) {
                JsonNode item = playlists.get(i);
                scanned++;

                if (isValidItem(item)) {
                    validItems.add(item);
                    if (validItems.size() == desiredLimit) {
                        break;
                    }
                }
            }

            if (offset + scanned >= total) {
                break;
            }

        }

        int newOffset = offset + scanned;

        ObjectNode patchedContentNode = objectMapper.createObjectNode();
        patchedContentNode.put("href", buildHref(query, "playlist", newOffset, desiredLimit));
        patchedContentNode.put("limit", desiredLimit);
        patchedContentNode.put("offset", newOffset);
        patchedContentNode.put("total", total);
        patchedContentNode.set("items", validItems);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("playlist" + "s", patchedContentNode);

        return root;
    }

    private List<String> validateTypesAsList(Set<String> types) {
        if (types == null) return List.of();
        return types.stream()
                .map(String::toLowerCase)
                .filter(CONTENT_TYPES::contains)
                .toList();
    }

    private boolean isValidItem(JsonNode item) {
        return !item.isNull() && item.hasNonNull("id") && item.hasNonNull("name");
    }

    private String buildHref(String query, String type, int offset, int limit) {
        return String.format("https://api.spotify.com/v1/search?q=%s&type=%s&limit=%d&offset=%d",
                URLEncoder.encode(query, StandardCharsets.UTF_8), type, limit, offset);
    }
}

