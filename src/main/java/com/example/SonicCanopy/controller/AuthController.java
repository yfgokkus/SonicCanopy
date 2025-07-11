package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.auth.AuthRequestDto;
import com.example.SonicCanopy.dto.auth.AuthResponseDto;
import com.example.SonicCanopy.dto.auth.RefreshTokenRequestDto;
import com.example.SonicCanopy.service.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request)
    {
        AuthResponseDto response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
