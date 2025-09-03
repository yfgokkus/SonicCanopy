package com.example.SonicCanopy.domain.dto.global;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Data
@Builder(builderClassName = "Builder", access = AccessLevel.PRIVATE)
public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String href;      // current page URL
    private String next;      // next page URL
    private String previous;  // previous page URL

    // Factory method to build PagedResponse with URLs
    public static <T> PagedResponse<T> of(
            List<T> items,
            int page,
            int size,
            long total,
            int totalPages,
            HttpServletRequest request
    ) {
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0;

        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequest(request);

        String hrefUrl = builder
                .replaceQueryParam("page", page)
                .replaceQueryParam("size", size)
                .toUriString();

        String nextUrl = hasNext
                ? builder.replaceQueryParam("page", page + 1).replaceQueryParam("size", size).toUriString()
                : null;

        String prevUrl = hasPrevious
                ? builder.replaceQueryParam("page", page - 1).replaceQueryParam("size", size).toUriString()
                : null;

        return PagedResponse.<T>builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .href(hrefUrl)
                .next(nextUrl)
                .previous(prevUrl)
                .build();
    }
}
