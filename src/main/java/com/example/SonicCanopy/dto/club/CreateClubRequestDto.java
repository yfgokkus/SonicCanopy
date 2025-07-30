package com.example.SonicCanopy.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record CreateClubRequestDto(
        @NotBlank String name,
        @Size(max = 255) String description,
        MultipartFile profilePicture
) {}
