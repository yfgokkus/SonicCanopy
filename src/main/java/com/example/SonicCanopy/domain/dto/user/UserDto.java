package com.example.SonicCanopy.domain.dto.user;

import lombok.Builder;

import java.util.Set;

@Builder
public record UserDto(
        Long id,
        String fullName,
        String username,
        String email,
        Set<String> roles
) {}