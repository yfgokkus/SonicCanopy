package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.dto.user.CreateUserRequest;
import com.example.SonicCanopy.domain.dto.user.UserDto;
import com.example.SonicCanopy.domain.mapper.UserMapper;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.ClubMemberService;
import com.example.SonicCanopy.service.app.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private final ClubMemberService clubMemberService;
    private final UserMapper mapper;

    public UserController(UserService userService, ClubMemberService clubMemberService, UserMapper mapper) {
        this.userService = userService;
        this.clubMemberService = clubMemberService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        UserDto response = mapper.toDto(user);

        log.info("User '{}' successfully created", request.username());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User successfully created", response));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable String username) {
        User user = userService.getByUsername(username);
        UserDto response = mapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe(@AuthenticationPrincipal User user) {
        User userFromDb = userService.getByUsername(user.getUsername());
        UserDto response = mapper.toDto(userFromDb);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/clubs")
    public ResponseEntity<ApiResponse<PagedResponse<ClubDto>>> getUserClubs(
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            HttpServletRequest request) {

        PagedResponse<ClubDto> result = clubMemberService.getUserClubs(user, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<String>> getAdminString() {
        return ResponseEntity.ok(ApiResponse.success("Access granted"));
    }
}

