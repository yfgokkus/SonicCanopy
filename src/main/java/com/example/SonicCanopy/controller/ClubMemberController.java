package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.ClubMemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/clubs/{clubId}")
public class ClubMemberController {
    private final ClubMemberService clubMemberService;

    public ClubMemberController(ClubMemberService clubMemberService) {
        this.clubMemberService = clubMemberService;
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Void>> joinClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user) {

        clubMemberService.joinClub(clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Join request submitted"));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user) {

        clubMemberService.leaveClub(clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Leave request submitted"));
    }

    @PostMapping("/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(@PathVariable Long clubId, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        clubMemberService.kickMember(clubId, userId, user);
        return ResponseEntity.ok(ApiResponse.success("User has been kicked from the club"));
    }

    @GetMapping("/join-requests")
    public ResponseEntity<ApiResponse<PagedResponse<ClubMemberDto>>> getJoinRequests(
            @PathVariable Long clubId,
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            HttpServletRequest request
    ) {
        PagedResponse<ClubMemberDto> page = clubMemberService.getAllJoinRequests(clubId, user, pageable, request);

        return ResponseEntity.ok(ApiResponse.success("Pending join requests retrieved successfully", page));
    }

    @PostMapping("/join-requests/{userId}")
    public ResponseEntity<ApiResponse<Void>> acceptJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User requester) {

        clubMemberService.acceptJoinRequest(clubId, userId, requester);
        return ResponseEntity.ok(ApiResponse.success("Join request accepted"));
    }

    @DeleteMapping("/join-requests/{userId}")
    public ResponseEntity<ApiResponse<Void>> rejectJoinRequest(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User requester) {

        clubMemberService.rejectJoinRequest(clubId, userId, requester);
        return ResponseEntity.ok(ApiResponse.success("Join request rejected"));
    }

}

