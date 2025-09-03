package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ClubAuthorizationService {
    
    private final ClubMemberRepository clubMemberRepository;
    
    public ClubAuthorizationService(ClubMemberRepository clubMemberRepository) {
        this.clubMemberRepository = clubMemberRepository;
    }

    public boolean isMember(Long clubId, Long userId) {
        return clubMemberRepository.existsById(new ClubMemberId(userId, clubId))
                && clubMemberRepository.findById(new ClubMemberId(userId, clubId))
                .map(member -> member.getStatus() == JoinStatus.APPROVED)
                .orElse(false);
    }

    public void authorizeMemberManagement(Long clubId, User requester) {
        ClubMember member = getMemberOrThrow(clubId, requester.getId());

        if (!member.getClubRole().canManageMembers()) {
            throw new UnauthorizedActionException("Not authorized to approve this request");
        }
    }

    public void authorizeEventManagement(Long clubId, User requester) {
        ClubMember member = getMemberOrThrow(clubId, requester.getId());

        if (!member.getClubRole().canManageEvents()) {
            throw new UnauthorizedActionException("Not authorized to manage events");
        }
    }

    private ClubMember getMemberOrThrow(Long clubId, Long userId) {
        return clubMemberRepository.findById(new ClubMemberId(userId, clubId))
                .orElseThrow(() -> new ClubMemberDoesNotExistException("User is not a member of this club"));
    }

}
