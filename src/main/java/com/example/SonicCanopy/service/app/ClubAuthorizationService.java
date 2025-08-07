package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.entities.*;
import com.example.SonicCanopy.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ClubAuthorizationService {
    
    private final ClubMemberRepository clubMemberRepository;
    
    public ClubAuthorizationService(ClubMemberRepository clubMemberRepository) {
        this.clubMemberRepository = clubMemberRepository;
    }

    private ClubMember isMember(Long clubId, Long userId) {
        ClubMember member = clubMemberRepository.findById(new ClubMemberId(userId, clubId))
                .orElseThrow(() -> new ClubMemberDoesNotExistException("User is not a member of this club"));

        JoinStatus status = member.getStatus();

        if (status !=  JoinStatus.APPROVED) {
            throw new UnauthorizedActionException("Not authorized to approve this request");
        }

        return member;
    }

    public void authorizeMemberManagement(Long clubId, User requester) {
        ClubMember member = isMember(clubId, requester.getId());

        ClubRole role = member.getClubRole();

        if (!role.canManageMembers()) {
            throw new UnauthorizedActionException("Not authorized to approve this request");
        }
    }

    public void authorizeEventManagement(Long clubId, User requester){
        ClubMember member = isMember(clubId, requester.getId());

        ClubRole role = member.getClubRole();

        if (!role.canManageEvents()) {
            throw new UnauthorizedActionException("Not authorized to approve this request");
        }
    }
    
}
