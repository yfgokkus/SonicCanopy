package com.example.SonicCanopy.service;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.domain.exception.clubMember.AlreadyMemberException;
import com.example.SonicCanopy.domain.exception.clubMember.ClubMemberDoesNotExistException;
import com.example.SonicCanopy.domain.exception.clubMember.RequestNotFoundException;
import com.example.SonicCanopy.domain.mapper.ClubMapper;
import com.example.SonicCanopy.domain.mapper.ClubMemberMapper;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.service.app.ClubAuthorizationService;
import com.example.SonicCanopy.service.app.ClubMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubMemberServiceTest {
    @Mock private  ClubMemberRepository clubMemberRepository;
    @Mock private  ClubRepository clubRepository;
    @Mock private  ClubMemberMapper clubMemberMapper;
    @Mock private  ClubAuthorizationService clubAuthorizationService;
    @Mock private  ClubMapper clubMapper;

    private ClubMemberService  clubMemberService;

    private User requester;
    private LocalDateTime fixedTime;

    private MockHttpServletRequest getAllMembersOrderedByRoleRequest;
    private MockHttpServletRequest getUserClubsRequest;
    private MockHttpServletRequest getAllJoinRequestsRequest;
    private Pageable pageable;
    @BeforeEach
    void setup(){
        Clock fixedClock = Clock.fixed(Instant.parse("2025-09-16T12:00:00Z"), ZoneOffset.UTC);
        fixedTime = LocalDateTime.now(fixedClock);

        clubMemberService = new ClubMemberService(
                clubMemberRepository,
                clubRepository,
                clubMemberMapper,
                clubAuthorizationService,
                clubMapper,
                fixedClock
        );

        requester = User.builder()
                .id(1L)
                .build();

        getAllMembersOrderedByRoleRequest = new MockHttpServletRequest();
        getAllMembersOrderedByRoleRequest.setRequestURI("/clubs/1/members");
        getAllMembersOrderedByRoleRequest.setQueryString("page=0&size=10");

        getUserClubsRequest = new MockHttpServletRequest();
        getUserClubsRequest.setRequestURI("/me/clubs");
        getUserClubsRequest.setQueryString("page=0&size=10");

        getAllJoinRequestsRequest = new MockHttpServletRequest();
        getAllJoinRequestsRequest.setRequestURI("/clubs/1/join-requests");
        getAllJoinRequestsRequest.setQueryString("page=0&size=10");

        pageable = PageRequest.of(0, 10);
    }

    // ---------- getAllMembersOrderedByRole tests ----------

    @Test
    void getAllMembersOrderedByRole_ShouldReturnPagedResponse_WhenMembersExist() {
        ClubMember member1 = new ClubMember();
        ClubMember member2 = new ClubMember();
        List<ClubMember> memberList = List.of(member1,member2);
        Page<ClubMember> clubMemberPage = new PageImpl<>(memberList, pageable, 1);

        ClubMemberDto memberDto1 = new ClubMemberDto(
                101L,
                "testuser",
                1L,
                "Test Club",
                ClubRole.MEMBER,
                JoinStatus.APPROVED,
                fixedTime
        );

        ClubMemberDto memberDto2 = new ClubMemberDto(
                102L,
                "testuser2",
                1L,
                "Test Club",
                ClubRole.ADMIN,
                JoinStatus.APPROVED,
                fixedTime
        );

        List<ClubMemberDto> dtoList = List.of(memberDto1,memberDto2);

        when(clubMemberRepository.findAllByClubIdOrderedByRole(requester.getId(), pageable))
                .thenReturn(clubMemberPage);

        when(clubMemberMapper.toDtoList(memberList)).thenReturn(dtoList);

        PagedResponse<ClubMemberDto> response = clubMemberService
                .getAllMembersOrderedByRole(requester, pageable, getAllMembersOrderedByRoleRequest);

        assertNotNull(response);

        assertEquals(2, response.getItems().size());
        assertEquals("testuser", response.getItems().get(0).username());
        assertEquals("testuser2", response.getItems().get(1).username());

        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2L, response.getTotal());
        assertFalse(response.isHasNext());

        verify(clubMemberRepository).findAllByClubIdOrderedByRole(requester.getId(), pageable);
        verify(clubMemberMapper).toDtoList(memberList);
    }

    @Test
    void getAllMembersOrderedByRole_ShouldReturnEmptyPagedResponse_WhenNoMembersExist() {
        Page<ClubMember> emptyPage = Page.empty(pageable);

        when(clubMemberRepository.findAllByClubIdOrderedByRole(requester.getId(), pageable))
                .thenReturn(emptyPage);

        when(clubMemberMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        PagedResponse<ClubMemberDto> response = clubMemberService
                .getAllMembersOrderedByRole(requester, pageable, getAllMembersOrderedByRoleRequest);

        assertNotNull(response);

        assertTrue(response.getItems().isEmpty());

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());

        verify(clubMemberRepository).findAllByClubIdOrderedByRole(requester.getId(), pageable);
        verify(clubMemberMapper).toDtoList(Collections.emptyList());
    }

    // ---------- getUserClubs tests ----------

    @Test
    void getUserClubs_ShouldReturnPagedClubs_WhenClubsExist() {
        Club club1 = Club.builder().id(1L).name("Coding Club").build();
        Club club2 = Club.builder().id(2L).name("Coding Club 2").build();

        List<Club> clubList = List.of(club1, club2);
        Page<Club> clubPage = new PageImpl<>(clubList, pageable, 1);

        ClubDto clubDto1 = new ClubDto(
                1L,
                "Coding Club",
                "A club for coders.",
                null,
                fixedTime);

        ClubDto clubDto2 = new ClubDto(
                2L,
                "Coding Club 2",
                "A club for coders.",
                null,
                fixedTime);

        List<ClubDto> clubDtoList = List.of(clubDto1, clubDto2);

        when(clubRepository.findByMembersUserId(requester.getId(), pageable)).thenReturn(clubPage);
        when(clubMapper.toDtoList(clubList)).thenReturn(clubDtoList);

        PagedResponse<ClubDto> response = clubMemberService.getUserClubs(requester, pageable, getUserClubsRequest);

        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals("Coding Club", response.getItems().getFirst().name());
        assertEquals("Coding Club 2", response.getItems().get(1).name());

        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2L, response.getTotal());
        assertFalse(response.isHasNext());

        verify(clubRepository).findByMembersUserId(1L, pageable);
        verify(clubMapper).toDtoList(clubList);
    }

    @Test
    void getUserClubs_ShouldReturnEmptyResponse_WhenUserHasNoClubs() {
        Page<Club> emptyPage = Page.empty(pageable);

        when(clubRepository.findByMembersUserId(requester.getId(), pageable)).thenReturn(emptyPage);
        when(clubMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        PagedResponse<ClubDto> response = clubMemberService.getUserClubs(requester, pageable, getUserClubsRequest);

        assertNotNull(response);
        assertTrue(response.getItems().isEmpty());

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());

        verify(clubRepository).findByMembersUserId(1L, pageable);
        verify(clubMapper).toDtoList(Collections.emptyList());
    }

    // ---------- getAllJoinRequests tests ----------

    @Test
    void getAllJoinRequests_ShouldReturnClubMemberDtoPage_WhenClubsExistAndRequesterIsAuthorized() {
        Long clubId = 1L;
        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);

        ClubMember clubMember1 = ClubMember.builder().id(new ClubMemberId(clubId,12L)).build();
        ClubMember clubMember2 = ClubMember.builder().id(new ClubMemberId(clubId,14L)).build();

        List<ClubMember> clubMemberList = List.of(clubMember1, clubMember2);
        Page<ClubMember> clubMemberPage = new PageImpl<>(clubMemberList, pageable, 2);

        ClubMemberDto clubMemberDto1 = new ClubMemberDto(
                12L,
                "user1",
                clubId,
                "game club",
                ClubRole.MEMBER,
                JoinStatus.APPROVED,
                fixedTime
        );
        ClubMemberDto clubMemberDto2 = new ClubMemberDto(
                14L,
                "user2",
                clubId,
                "game club",
                null,
                JoinStatus.PENDING,
                null
        );
        List<ClubMemberDto> clubMemberDtoList = List.of(clubMemberDto1, clubMemberDto2);

        when(clubMemberRepository.findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable))
                .thenReturn(clubMemberPage);

        when(clubMemberMapper.toDtoList(clubMemberPage.getContent())).thenReturn(clubMemberDtoList);

        PagedResponse<ClubMemberDto> response = clubMemberService
                .getAllJoinRequests(clubId, requester, pageable, getAllJoinRequestsRequest);

        assertNotNull(response);

        assertEquals(2, response.getItems().size());
        assertEquals("user1", response.getItems().get(0).username());
        assertEquals("user2", response.getItems().get(1).username());

        assertEquals(0, response.getPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2L, response.getTotal());
        assertFalse(response.isHasNext());

        verify(clubMemberRepository).findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable);
        verify(clubMemberMapper).toDtoList(clubMemberList);
    }

    @Test
    void getAllJoinRequests_ShouldThrowRequestNotFoundException_WhenNoJoinRequestsExist() {
        Long clubId = 1L;
        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);

        when(clubMemberRepository.findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable))
                .thenReturn(Page.empty());

        assertThrows(RequestNotFoundException.class,
                () -> clubMemberService.getAllJoinRequests(clubId, requester, pageable, getAllJoinRequestsRequest),
                "Should throw RequestNotFoundException when there are no pending join requests");

        verify(clubMemberRepository).findByClubIdAndStatus(clubId, JoinStatus.PENDING, pageable);
        verify(clubMemberMapper, never()).toDtoList(any());
    }

    // ---------- joinClub tests ----------

    @Test
    void joinClub_ShouldSaveMemberWithApprovedStatus_WhenClubIsPublicAndUserNotMember() {
        Long clubId = 1L;
        Club club = Club.builder().id(clubId).name("Public Club").description("desc").build();
        club.setPrivate(false);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClub(requester, club)).thenReturn(false);

        clubMemberService.joinClub(clubId, requester);

        ArgumentCaptor<ClubMember> memberCaptor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(memberCaptor.capture());

        ClubMember saved = memberCaptor.getValue();
        assertEquals(clubId, saved.getClub().getId());
        assertEquals(requester.getId(), saved.getUser().getId());
        assertEquals(JoinStatus.APPROVED, saved.getStatus(), "Public club join should be APPROVED");
    }

    @Test
    void joinClub_ShouldSaveMemberWithPendingStatus_WhenClubIsPrivateAndUserNotMember() {
        Long clubId = 2L;
        Club club = Club.builder().id(clubId).name("Private Club").description("desc").build();
        club.setPrivate(true);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClub(requester, club)).thenReturn(false);

        clubMemberService.joinClub(clubId, requester);

        ArgumentCaptor<ClubMember> captor = ArgumentCaptor.forClass(ClubMember.class);
        verify(clubMemberRepository).save(captor.capture());

        ClubMember saved = captor.getValue();
        assertEquals(JoinStatus.PENDING, saved.getStatus(), "Private club join should be PENDING");
    }

    @Test
    void joinClub_ShouldThrowClubNotFoundException_WhenClubDoesNotExist() {
        Long clubId = 99L;
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class,
                () -> clubMemberService.joinClub(clubId, requester),
                "Should throw if club is not found");

        verify(clubMemberRepository, never()).existsByUserAndClub(any(), any());
        verify(clubMemberRepository, never()).save(any());
    }

    @Test
    void joinClub_ShouldThrowAlreadyMemberException_WhenUserAlreadyMemberOrPending() {
        Long clubId = 1L;
        Club club = Club.builder().id(clubId).name("Any Club").description("desc").build();

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubMemberRepository.existsByUserAndClub(requester, club)).thenReturn(true);

        assertThrows(AlreadyMemberException.class,
                () -> clubMemberService.joinClub(clubId, requester),
                "Should throw if user already joined or has a pending request");

        verify(clubMemberRepository, never()).save(any());
    }

    // ---------- leaveClub tests ----------

    @Test
    void leaveClub_ShouldDeleteMember_WhenUserIsMember() {
        Long clubId = 1L;
        ClubMember member = ClubMember.builder()
                .id(new ClubMemberId(clubId, requester.getId()))
                .club(Club.builder().id(clubId).build())
                .user(requester)
                .build();

        when(clubMemberRepository.findByClubIdAndUserId(clubId, requester.getId()))
                .thenReturn(Optional.of(member));

        clubMemberService.leaveClub(clubId, requester);

        verify(clubMemberRepository).delete(member);
    }

    @Test
    void leaveClub_ShouldThrowException_WhenUserIsNotMember() {
        Long clubId = 1L;
        when(clubMemberRepository.findByClubIdAndUserId(clubId, requester.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ClubMemberDoesNotExistException.class,
                () -> clubMemberService.leaveClub(clubId, requester),
                "Should throw if user is not a member of the club");

        verify(clubMemberRepository, never()).delete(any());
    }

    // ---------- acceptJoinRequest tests ----------

    @Test
    void acceptJoinRequest_shouldApproveAndSaveMembership_whenAuthorizedAndPendingRequestExists() {
        Long clubId = 1L;
        Long userId = 2L;
        ClubMember membership = ClubMember.builder()
                .id(new ClubMemberId(clubId, userId))
                .status(JoinStatus.PENDING)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING))
                .thenReturn(Optional.of(membership));

        clubMemberService.acceptJoinRequest(clubId, userId, requester);

        assertEquals(JoinStatus.APPROVED, membership.getStatus());
        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository).save(membership);
    }

    @Test
    void acceptJoinRequest_shouldThrow_whenPendingRequestDoesNotExist() {
        Long clubId = 1L;
        Long userId = 2L;

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING))
                .thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class,
                () -> clubMemberService.acceptJoinRequest(clubId, userId, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).save(any());
    }

    // ---------- acceptJoinRequest tests ----------

    @Test
    void rejectJoinRequest_shouldDeleteMembership_whenAuthorizedAndPendingRequestExists() {
        Long clubId = 1L;
        Long userId = 2L;
        ClubMember membership = ClubMember.builder()
                .id(new ClubMemberId(clubId, userId))
                .status(JoinStatus.PENDING)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING))
                .thenReturn(Optional.of(membership));

        clubMemberService.rejectJoinRequest(clubId, userId, requester);

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository).delete(membership);
    }

    @Test
    void rejectJoinRequest_shouldThrow_whenPendingRequestDoesNotExist() {
        Long clubId = 1L;
        Long userId = 2L;

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findByClubIdAndUserIdAndStatus(clubId, userId, JoinStatus.PENDING))
                .thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class,
                () -> clubMemberService.rejectJoinRequest(clubId, userId, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

    // ---------- kickMember tests ----------

    @Test
    void kickMember_shouldDeleteTargetMember_whenAuthorizedAndValidConditions() {
        Long clubId = 1L;
        Long userIdToKick = 2L;
        ClubMember targetMember = ClubMember.builder()
                .id(new ClubMemberId(userIdToKick, clubId))
                .clubRole(ClubRole.MEMBER)
                .build();
        ClubMember requesterMember = ClubMember.builder()
                .id(new ClubMemberId(requester.getId(), clubId))
                .clubRole(ClubRole.ADMIN)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))).thenReturn(Optional.of(targetMember));
        when(clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))).thenReturn(Optional.of(requesterMember));

        clubMemberService.kickMember(clubId, userIdToKick, requester);

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository).delete(targetMember);
    }

    @Test
    void kickMember_shouldThrow_whenRequesterTriesToKickSelf() {
        Long clubId = 1L;
        Long userIdToKick = requester.getId();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);

        assertThrows(UnauthorizedActionException.class,
                () -> clubMemberService.kickMember(clubId, userIdToKick, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

    @Test
    void kickMember_shouldThrow_whenTargetMemberDoesNotExist() {
        Long clubId = 1L;
        Long userIdToKick = 2L;

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))).thenReturn(Optional.empty());

        assertThrows(ClubMemberDoesNotExistException.class,
                () -> clubMemberService.kickMember(clubId, userIdToKick, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

    @Test
    void kickMember_shouldThrow_whenRequesterIsNotMember() {
        Long clubId = 1L;
        Long userIdToKick = 2L;
        ClubMember targetMember = ClubMember.builder()
                .id(new ClubMemberId(userIdToKick, clubId))
                .clubRole(ClubRole.MEMBER)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))).thenReturn(Optional.of(targetMember));
        when(clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))).thenReturn(Optional.empty());

        assertThrows(ClubMemberDoesNotExistException.class,
                () -> clubMemberService.kickMember(clubId, userIdToKick, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

    @Test
    void kickMember_shouldThrow_whenTargetIsOwner() {
        Long clubId = 1L;
        Long userIdToKick = 2L;
        ClubMember targetMember = ClubMember.builder()
                .id(new ClubMemberId(userIdToKick, clubId))
                .clubRole(ClubRole.OWNER)
                .build();
        ClubMember requesterMember = ClubMember.builder()
                .id(new ClubMemberId(requester.getId(), clubId))
                .clubRole(ClubRole.ADMIN)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))).thenReturn(Optional.of(targetMember));
        when(clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))).thenReturn(Optional.of(requesterMember));

        assertThrows(UnauthorizedActionException.class,
                () -> clubMemberService.kickMember(clubId, userIdToKick, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

    @Test
    void kickMember_shouldThrow_whenTargetIsAdminAndRequesterNotOwner() {
        Long clubId = 1L;
        Long userIdToKick = 2L;
        ClubMember targetMember = ClubMember.builder()
                .id(new ClubMemberId(userIdToKick, clubId))
                .clubRole(ClubRole.ADMIN)
                .build();
        ClubMember requesterMember = ClubMember.builder()
                .id(new ClubMemberId(requester.getId(), clubId))
                .clubRole(ClubRole.ADMIN)
                .build();

        doNothing().when(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        when(clubMemberRepository.findById(new ClubMemberId(userIdToKick, clubId))).thenReturn(Optional.of(targetMember));
        when(clubMemberRepository.findById(new ClubMemberId(requester.getId(), clubId))).thenReturn(Optional.of(requesterMember));

        assertThrows(UnauthorizedActionException.class,
                () -> clubMemberService.kickMember(clubId, userIdToKick, requester));

        verify(clubAuthorizationService).authorizeMemberManagement(clubId, requester);
        verify(clubMemberRepository, never()).delete(any());
    }

}
