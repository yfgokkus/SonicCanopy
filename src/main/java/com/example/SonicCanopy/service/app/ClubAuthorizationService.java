package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.clubMember.ClubRoleNotFoundException;
import com.example.SonicCanopy.repository.ClubMemberRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClubAuthorizationService {

    private final ClubMemberRepository clubMemberRepository;

    public ClubAuthorizationService(ClubMemberRepository clubMemberRepository) {
        this.clubMemberRepository = clubMemberRepository;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMember(Long clubId, Long userId) {
        return clubMemberRepository.findById(new ClubMemberId(userId, clubId))
                .map(member -> member.getStatus() == JoinStatus.APPROVED)
                .orElse(false);
    }

    public void isMemberOrThrow(Long clubId, Long userId, String message){
        if(!isMember(userId,  clubId)) {
            throw new UnauthorizedActionException(message);
        }
    }

    public void authorize(Long clubId, Long requesterId, Privilege privilege) {
        ClubRole role = getClubRole(clubId, requesterId);

        if (!role.allowedTo(privilege)) {
            throw new UnauthorizedActionException("Not authorized for: " + privilege);
        }
    }

    private ClubRole getClubRole(Long clubId, Long userId) {
        return clubMemberRepository.findClubRoleByClubIdAndUserId(clubId, userId)
            .orElseThrow(() -> {
                log.error("Error fetching club role by clubId={} and userId={}", clubId, userId);
                return new ClubRoleNotFoundException("Cannot verify permissions");
            });
    }
}
