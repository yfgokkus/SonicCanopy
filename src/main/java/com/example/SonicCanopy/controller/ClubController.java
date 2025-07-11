package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.dto.user.CreateUserRequestDto;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.service.app.ClubService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs")
@Slf4j
public class ClubController {
    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    public ResponseEntity<ClubDto> createClub(
            @Valid @RequestBody CreateClubRequestDto createClubRequestDto,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        ClubDto createdClub = clubService.createClub(createClubRequestDto, currentUser);
        return ResponseEntity.ok(createdClub);
    }



}
