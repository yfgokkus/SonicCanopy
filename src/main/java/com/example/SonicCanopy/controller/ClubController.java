package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.club.ClubSearchResultDto;
import com.example.SonicCanopy.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.service.app.ClubService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@Slf4j
public class ClubController {
    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClubDto> createClub(
            @ModelAttribute @Valid CreateClubRequestDto dto,
            @AuthenticationPrincipal User user) {

        ClubDto created = clubService.createClub(dto, user);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<Void> deleteClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {
        clubService.deleteClub(clubId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubs/user")
    public ResponseEntity<List<ClubDto>> getUserClubs(@AuthenticationPrincipal User user) {
        List<ClubDto> clubs = clubService.getUserClubs(user.getId());
        return ResponseEntity.ok(clubs);
    }

    @PostMapping("/{clubId}/profile-picture")
    public ResponseEntity<String> uploadClubImage(
            @PathVariable Long clubId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        String imageUrl = clubService.updateClubImage(clubId, file, user);
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/{clubId}/profile-picture")
    public ResponseEntity<Void> deleteClubImage(@PathVariable Long clubId, @AuthenticationPrincipal User requester) {
        clubService.deleteClubImage(clubId, requester);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ClubSearchResultDto> searchClubs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ClubSearchResultDto response = clubService.searchClubs(query, page, size);
        return ResponseEntity.ok(response);
    }
}
