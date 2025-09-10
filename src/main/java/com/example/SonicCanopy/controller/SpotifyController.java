package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.spotify.MultiTypeContentDto;
import com.example.SonicCanopy.domain.dto.spotify.PagedSpotifyContent;
import com.example.SonicCanopy.service.infrastructure.spotify.SpotifySearchManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifySearchManager spotifySearchManager;

    public SpotifyController(SpotifySearchManager spotifySearchManager) {
        this.spotifySearchManager = spotifySearchManager;
    }

    @GetMapping("/search")
    public ApiResponse<?> searchContent(@RequestParam String q,
                                           @RequestParam(required = false) Set<String> typeSet,
                                           @RequestParam(defaultValue = "0") int offset,
                                           @RequestParam(defaultValue = "10") int limit) {
        PagedSpotifyContent<?> result = spotifySearchManager.search(q, typeSet, offset, limit);
        return ApiResponse.success(result);
    }

    @GetMapping("/preview")
    public ApiResponse<MultiTypeContentDto> getSearchPreview(@RequestParam String q,
                                                                             @RequestParam(required = false) Set<String> typeSet) {
        MultiTypeContentDto result = spotifySearchManager.shallowSearch(q, typeSet);
        return ApiResponse.success(result);
    }

}
