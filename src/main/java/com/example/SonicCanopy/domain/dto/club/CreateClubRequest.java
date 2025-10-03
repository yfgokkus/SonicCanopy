package com.example.SonicCanopy.domain.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record CreateClubRequest(
        @NotBlank String name,
        @Size(max = 255) String description,
        Boolean imageProvided,
        MultipartFile image
) {}
