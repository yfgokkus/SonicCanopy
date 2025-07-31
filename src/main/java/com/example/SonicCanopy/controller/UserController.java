package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.user.CreateUserRequestDto;
import com.example.SonicCanopy.dto.user.UserDto;
import com.example.SonicCanopy.mapper.UserMapper;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.service.app.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;

    public UserController(UserService userService, UserMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        User user = userService.createUser(request);
        UserDto response = mapper.toUserDto(user);

        log.info("User '{}' successfully created", request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        User userDromDb = userService.getByUsername(user.getUsername());
        return ResponseEntity.ok(mapper.toUserDto(userDromDb));
    }

    @GetMapping("/admin")
    public String getAdminString(){
        return "This is the Admin";
    }

}

