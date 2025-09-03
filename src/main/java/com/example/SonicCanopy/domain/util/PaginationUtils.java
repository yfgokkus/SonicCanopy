package com.example.SonicCanopy.domain.util;

import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public class PaginationUtils {

    private PaginationUtils() {}

    public static <T> PagedResponse<T> buildPagedResponse(List<T> items, Page<?> page, HttpServletRequest request) {
        return PagedResponse.of(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                request
        );
    }
}
