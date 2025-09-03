package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.user.UserDto;
import com.example.SonicCanopy.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
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

    public List<UserDto> toDtoList(List<User> users) {
        return users.stream().map(this::toDto).toList();
    }
}