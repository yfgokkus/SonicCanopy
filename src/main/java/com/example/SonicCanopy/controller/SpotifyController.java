package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.service.spotify.SpotifySearchManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifySearchManager spotifySearchManager;

    public SpotifyController(SpotifySearchManager spotifySearchManager) {
        this.spotifySearchManager = spotifySearchManager;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchContent(@RequestParam String q,
                                           @RequestParam(required = false) Set<String> typeSet,
                                           @RequestParam(defaultValue = "0") int offset,
                                           @RequestParam(defaultValue = "10") int limit) {
        Object result = spotifySearchManager.search(q, typeSet, offset, limit);
        return ResponseEntity.ok(result);
    }


//    @GetMapping("/search")
//    public ResponseEntity<JsonNode> searchContent(@RequestParam String q,
//                                                     @RequestParam(required = false) String type) {
//        String searchType = (type != null && !type.isBlank()) ? type : "playlist";
//        JsonNode response = spotifyClient.search(q, searchType);
//        return ResponseEntity.ok(response);
//    }


}
