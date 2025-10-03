package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.club.CreateClubRequest;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.ClubService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/clubs")
@Slf4j
public class ClubController {
    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ClubDto>> createClub(
            @RequestBody @Valid CreateClubRequest dto,
            @AuthenticationPrincipal User user) {

        ClubDto created = clubService.createClub(dto, user);
        return ResponseEntity.ok(ApiResponse.success("Club created successfully", created));
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<ApiResponse<Void>> deleteClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {
        clubService.deleteClub(clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Club deleted successfully"));

    }

    @PostMapping("/{clubId}/profile-picture")
    public ResponseEntity<ApiResponse<String>> uploadClubImage(
            @PathVariable Long clubId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        String imageUrl = clubService.uploadClubImage(clubId, file, user);
        return ResponseEntity.ok(ApiResponse.success("Profile picture updated", imageUrl));

    }

    @DeleteMapping("/{clubId}/profile-picture")
    public ResponseEntity<ApiResponse<Void>> deleteClubImage(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User requester
    ) {
        clubService.deleteClubImage(clubId, requester);
        return ResponseEntity.ok(ApiResponse.success("Profile picture deleted"));

    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ClubDto>>> searchClubs(
            @RequestParam String query,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            HttpServletRequest request
    ) {
        PagedResponse<ClubDto> response = clubService.searchClubs(query, pageable, request);

        return ResponseEntity.ok(ApiResponse.success("Search results", response));
    }

    @DeleteMapping("/{clubId}")
    public ResponseEntity<ApiResponse<Void>> toggleClubPrivacy(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user
    ) {
        clubService.togglePrivacy(clubId, user);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
