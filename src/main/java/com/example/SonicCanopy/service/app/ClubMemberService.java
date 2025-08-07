package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.entities.*;
import com.example.SonicCanopy.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.exception.clubMember.RequestNotFoundException;
import com.example.SonicCanopy.mapper.ClubMapper;
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
    private final ClubAuthorizationService clubAuthorizationService;
    private final ClubMapper clubMapper;

    public ClubMemberService(ClubMemberRepository clubMemberRepository, ClubRepository clubRepository, ClubMemberMapper clubMemberMapper, ClubAuthorizationService clubAuthorizationService, ClubMapper clubMapper) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubRepository = clubRepository;
        this.clubMemberMapper = clubMemberMapper;
        this.clubAuthorizationService = clubAuthorizationService;
        this.clubMapper = clubMapper;
    }

    public Page<ClubMemberDto> getAllMembersOrderedByRole(User user, Pageable pageable) {
        //Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending()); // creating pageable object

        Page<ClubMember> clubMembers = clubMemberRepository.findAllByClubIdOrderedByRole(user.getId(), pageable);

        return clubMembers.map(clubMemberMapper::toDto);
    }

    public Page<ClubDto> getUserClubs(User user, Pageable pageable) {
        Page<Club> userClubsPage = clubMemberRepository.findClubsByUserId(user.getId(), pageable);
        return userClubsPage.map(clubMapper::toDto);
    }

    public Page<ClubMemberDto> getAllJoinRequests(Long clubId, User requester, Pageable pageable) {
        clubAuthorizationService.authorizeMemberManagement(clubId, requester);

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

        clubAuthorizationService.authorizeMemberManagement(clubId, requester);

        ClubMember membership = getJoinRequestOrThrow(clubId, userId);

        membership.setStatus(JoinStatus.APPROVED);
        clubMemberRepository.save(membership);
    }

    public void rejectJoinRequest(Long clubId, Long userId, User requester) {
        clubAuthorizationService.authorizeMemberManagement(clubId, requester);
        ClubMember membership = getJoinRequestOrThrow(clubId, userId);

        clubMemberRepository.delete(membership);
    }

    public void kickMember(Long clubId, Long userIdToKick, User requester) {
        clubAuthorizationService.authorizeMemberManagement(clubId, requester);

        if (requester.getId().equals(userIdToKick)) {
            throw new UnauthorizedActionException("You cannot kick yourself");
        }

        ClubMember targetMember = clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))
                .orElseThrow(() -> new ClubMemberDoesNotExistException("User is not a member of this club"));

        ClubMember requesterMember = clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))
                .orElseThrow(() -> new ClubMemberDoesNotExistException("You are not a member of this club"));

        if (targetMember.getClubRole() == ClubRole.OWNER) {
            throw new UnauthorizedActionException("You cannot kick the club owner");
        }

        if (targetMember.getClubRole() == ClubRole.ADMIN && requesterMember.getClubRole() != ClubRole.OWNER) {
            throw new UnauthorizedActionException("Only the owner can kick an admin");
        }

        clubMemberRepository.delete(targetMember);
    }

    private ClubMember getJoinRequestOrThrow(Long clubId, Long userId) {
        return clubMemberRepository
                .findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING)
                .orElseThrow(() -> new RequestNotFoundException("There is no pending join request for this user"));
    }
}
