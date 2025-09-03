package com.example.SonicCanopy.domain.dto.clubMember;

import com.example.SonicCanopy.domain.entity.ClubRole;
import com.example.SonicCanopy.domain.entity.JoinStatus;

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
