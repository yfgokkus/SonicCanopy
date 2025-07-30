package com.example.SonicCanopy.dto.club;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubSearchResultDto {
    private List<ClubDto> results;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean isFirst;
    private boolean isLast;
}
