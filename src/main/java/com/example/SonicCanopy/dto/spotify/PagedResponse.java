package com.example.SonicCanopy.dto.spotify;

import lombok.Data;

import java.util.List;

@Data
public class PagedResponse<T> {
    private List<T> items;
    private int offset;
    private int limit;
    private int total;
    private boolean hasNext;

    public PagedResponse(List<T> items, int offset, int limit, int total) {
        this.items = items;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
        this.hasNext = offset + limit < total;
    }

}
