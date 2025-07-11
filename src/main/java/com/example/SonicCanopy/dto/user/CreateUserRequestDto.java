package com.example.SonicCanopy.dto.user;

import com.example.SonicCanopy.entities.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateUserRequestDto(
        @NotBlank(message = "Name cannot be blank") String fullName,
        @NotBlank(message = "Username cannot be blank") String username,
        @NotBlank(message = "Password cannot be blank") String password,
        @NotNull(message = "Authorities cannot be null")
        @NotEmpty(message = "Authorities cannot be empty")
        Set<@Valid Role> authorities
) {}
