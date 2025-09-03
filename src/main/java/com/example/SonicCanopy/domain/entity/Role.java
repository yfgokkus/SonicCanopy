package com.example.SonicCanopy.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;

public enum Role implements GrantedAuthority {
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    public String getAuthority() {
        return name();
    }

    @JsonCreator // used when deserializing from JSON
    public static Role fromValue(String value) {
        return Arrays.stream(Role.values())
                .filter(role -> role.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + value));
    }
}

