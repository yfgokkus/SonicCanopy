package com.example.SonicCanopy.domain.dto.spotify;

import lombok.Data;

import java.util.List;

@Data
public class PagedSpotifyContent <T> {
    private List<T> items;
    private int offset;
    private int limit;
    private long total;

    public PagedSpotifyContent(){}

    public PagedSpotifyContent(List<T> items, int offset, int limit, long total) {
        this.items = items;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
        //TODO: next and previous
    }
}