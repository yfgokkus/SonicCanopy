package com.example.SonicCanopy.domain.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateCommentRequest (
        @NotBlank @Size(max = 1000)
        String content,
        Long parentId //nullable
){}