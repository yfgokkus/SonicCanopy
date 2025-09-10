package com.example.SonicCanopy.service.infrastructure.spotify;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
@Slf4j
public class SpotifyClient {

    private final WebClient spotifyClient;
    private final SpotifyAuthService authService;

    public SpotifyClient(WebClient.Builder webClientBuilder, SpotifyAuthService authService) {
        this.spotifyClient = webClientBuilder
                .baseUrl("https://api.spotify.com/v1")
                .build();
        this.authService = authService;
    }

    public JsonNode search(String query, String type, int offset, int limit) {
        String token = authService.getAccessToken();

        return spotifyClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("type", type)
                        .queryParam("offset", offset)
                        .queryParam("limit", limit)
                        .queryParam("limit", 5)
                        .queryParam("market", "US")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getAlbum(String id) {
        return requestBuilder("albums", id);
    }

    public JsonNode getTrack(String id) {
        return requestBuilder("tracks", id);
    }

    public JsonNode getPlaylist(String id) { return requestBuilder("playlists", id); }

    public JsonNode getArtist(String id) {
        return requestBuilder("artists", id);
    }

    private JsonNode requestBuilder(String type, String id) {
        String token = authService.getAccessToken();

        return spotifyClient.get()
                .uri("/{type}/{id}", type, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }



}
