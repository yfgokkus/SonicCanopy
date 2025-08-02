package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.dto.response.ApiResponse;
import com.example.SonicCanopy.entities.ClubMember;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.service.app.ClubMemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clubs/{clubId}")
public class ClubMemberController {
    private final ClubMemberService clubMemberService;

    public ClubMemberController(ClubMemberService clubMemberService) {
        this.clubMemberService = clubMemberService;
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> joinClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user) {

        clubMemberService.joinClub(clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Join request submitted"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> leaveClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user) {

        clubMemberService.leaveClub(clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Leave request submitted"));
    }

    @GetMapping("/join-requests")
    public ResponseEntity<ApiResponse<Page<ClubMemberDto>>> getJoinRequests(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {

        Page<ClubMemberDto> page = clubMemberService.getAllJoinRequests(clubId, user, pageable);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("page", page.getNumber());
        metadata.put("size", page.getSize());
        metadata.put("totalElements", page.getTotalElements());
        metadata.put("totalPages", page.getTotalPages());

        return ResponseEntity.ok(
                ApiResponse.success("Pending join requests retrieved successfully", page, metadata)
        );
    }

    @PostMapping("/join-requests/{userId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User requester) {

        clubMemberService.acceptJoinRequest(clubId, userId, requester);
        return ResponseEntity.ok(ApiResponse.success("Join request accepted"));
    }

    @DeleteMapping("/join-requests/{userId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User requester) {

        clubMemberService.rejectJoinRequest(clubId, userId, requester);
        return ResponseEntity.ok(ApiResponse.success("Join request rejected"));
    }

}

