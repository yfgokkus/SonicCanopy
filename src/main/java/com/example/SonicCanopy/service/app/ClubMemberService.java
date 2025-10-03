package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.domain.exception.clubMember.RequestNotFoundException;
import com.example.SonicCanopy.domain.mapper.ClubMapper;
import com.example.SonicCanopy.domain.mapper.ClubMemberMapper;
import com.example.SonicCanopy.domain.util.PaginationUtils;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClubMemberService {
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubAuthorizationService clubAuthorizationService;
    private final ClubMapper clubMapper;

    private final Clock clock;

    public ClubMemberService(ClubMemberRepository clubMemberRepository,
                             ClubRepository clubRepository,
                             ClubMemberMapper clubMemberMapper,
                             ClubAuthorizationService clubAuthorizationService,
                             ClubMapper clubMapper,
                             Clock clock) {
        this.clubMemberRepository = clubMemberRepository;
        this.clubRepository = clubRepository;
        this.clubMemberMapper = clubMemberMapper;
        this.clubAuthorizationService = clubAuthorizationService;
        this.clubMapper = clubMapper;
        this.clock = clock;
    }

    public PagedResponse<ClubMemberDto> getAllMembersOrderedByRole(User user,
                                                                   Pageable pageable,
                                                                   HttpServletRequest request) {

        Page<ClubMember> clubMemberPage = clubMemberRepository
                .findAllByClubIdOrderedByRole(user.getId(), pageable);

        List<ClubMemberDto> clubMembers = clubMemberMapper.toDtoList(clubMemberPage.getContent());

        return PaginationUtils.buildPagedResponse(clubMembers, clubMemberPage, request);
    }

    public PagedResponse<ClubDto> getUserClubs(User user, Pageable pageable, HttpServletRequest request) {
        Page<Club> userClubsPage = clubRepository.findByMembersUserId(user.getId(), pageable);
        List<ClubDto> userClubs = clubMapper.toDtoList(userClubsPage.getContent());
        return PaginationUtils.buildPagedResponse(userClubs, userClubsPage, request);
    }

    public PagedResponse<ClubMemberDto> getAllJoinRequests(Long clubId,
                                                           User requester,
                                                           Pageable pageable,
                                                           HttpServletRequest request) {
        clubAuthorizationService.authorizeMemberManagement(clubId, requester);

        Page<ClubMember> pendingRequestsPage = clubMemberRepository
                .findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable);

        if (pendingRequestsPage.isEmpty()) {
            throw new RequestNotFoundException("No join requests found");
        }

        List<ClubMemberDto> pendingRequests = clubMemberMapper.toDtoList(pendingRequestsPage.getContent());

        return PaginationUtils.buildPagedResponse(pendingRequests, pendingRequestsPage, request);
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
                .joinedAt(LocalDateTime.now(clock))
                .build();

        clubMemberRepository.save(clubMember);
    }

    public void leaveClub(Long clubId, User user) {
        ClubMember member = clubMemberRepository.findByClubIdAndUserId(clubId, user.getId())
                        .orElseThrow(
                                () -> new ClubMemberDoesNotExistException("You are not a member of this club")
                        );

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
