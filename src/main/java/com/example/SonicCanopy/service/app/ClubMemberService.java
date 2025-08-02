package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.entities.*;
import com.example.SonicCanopy.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.exception.clubMember.RequestNotFoundException;
import com.example.SonicCanopy.mapper.ClubMemberMapper;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberMapper clubMemberMapper;

    public ClubMemberService(ClubMemberRepository clubMemberRepository, ClubRepository clubRepository, ClubMemberMapper clubMemberMapper) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubRepository = clubRepository;
        this.clubMemberMapper = clubMemberMapper;
    }

    public Page<ClubMemberDto> getAllJoinRequests(Long clubId, User requester, Pageable pageable) {
        verifyClubAuthority(clubId, requester);

        Page<ClubMember> pendingRequestsPage = clubMemberRepository.findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable);

        if (pendingRequestsPage.isEmpty()) {
            throw new RequestNotFoundException("No join requests found");
        }

        return pendingRequestsPage.map(clubMemberMapper::toDto);
    }

    public void joinClub(Long clubId, User user) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (clubMemberRepository.existsByUserAndClub(user, club)) {
            throw new AlreadyMemberException("You already joined or requested to join this club");
        }

        ClubMember clubMember = ClubMember.builder()
                .id(new ClubMemberId(user.getId(), club.getId()))
                .user(user)
                .club(club)
                .clubRole(ClubRole.MEMBER)
                .status(club.isPrivate() ? JoinStatus.PENDING : JoinStatus.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build();

        clubMemberRepository.save(clubMember);
    }

    public void leaveClub(Long clubId, User user) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, user.getId())
                        .orElseThrow(() -> new ClubMemberDoesNotExistException("You are not a member of this club"));

        clubMemberRepository.delete(member);
    }

    public void acceptJoinRequest(Long clubId, Long userId, User requester) {

        verifyClubAuthority(clubId, requester);

        ClubMember membership = getJoinRequestOrThrow(clubId, userId);

        membership.setStatus(JoinStatus.APPROVED);
        clubMemberRepository.save(membership);
    }

    public void rejectJoinRequest(Long clubId, Long userId, User requester) {
        verifyClubAuthority(clubId, requester);
        ClubMember membership = getJoinRequestOrThrow(clubId, userId);

        clubMemberRepository.delete(membership);
    }

    private ClubMember getJoinRequestOrThrow(Long clubId, Long userId) {
        return clubMemberRepository
                .findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING)
                .orElseThrow(() -> new RequestNotFoundException("There is no pending join request for this user"));
    }

    private void verifyClubAuthority(Long clubId, User requester) {
        ClubMember member = clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))
                .orElseThrow(() -> new ClubMemberDoesNotExistException("You are not a member of this club"));

        ClubRole role = member.getClubRole();

        if (role != ClubRole.ADMIN && role != ClubRole.OWNER) {
            throw new UnauthorizedActionException("Not authorized to approve this request");
        }

        if (member.getStatus() != JoinStatus.APPROVED) {
                 throw new UnauthorizedActionException("Membership not approved");
        }
    }
}
