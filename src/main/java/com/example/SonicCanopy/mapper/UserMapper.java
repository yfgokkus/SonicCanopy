package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.user.UserDto;
import com.example.SonicCanopy.entities.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        Set<String> roles = user.getAuthorities().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}