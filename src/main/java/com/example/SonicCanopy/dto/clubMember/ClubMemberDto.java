package com.example.SonicCanopy.dto.clubMember;

import com.example.SonicCanopy.entities.ClubRole;
import com.example.SonicCanopy.entities.JoinStatus;

import java.time.LocalDateTime;

public record ClubMemberDto(
        Long userId,
        String username,
        Long clubId,
        String clubName,
        ClubRole clubRole,
        JoinStatus status,
        LocalDateTime joinedAt
) {}
