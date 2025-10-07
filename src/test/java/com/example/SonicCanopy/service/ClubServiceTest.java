package com.example.SonicCanopy.service;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.club.CreateClubRequest;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.mapper.ClubMapper;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.service.app.ClubAuthorizationService;
import com.example.SonicCanopy.service.app.ClubService;
import com.example.SonicCanopy.service.infrastructure.firebase.concretes.FirebaseStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock private ClubRepository clubRepository;
    @Mock private ClubMapper clubMapper;
    @Mock private FirebaseStorageService firebaseStorageService;
    @Mock private ClubAuthorizationService clubAuthorizationService;
    @Mock private ClubMemberRepository clubMemberRepository;

    private ClubService clubService;

    private User creator;

    private LocalDateTime fixedTime;


    @BeforeEach
    void setup(){
        Clock fixedClock = Clock.fixed(Instant.parse("2025-09-16T12:00:00Z"), ZoneOffset.UTC);
        fixedTime = LocalDateTime.now(fixedClock);
        clubService = new ClubService(clubRepository,
                clubMapper,
                firebaseStorageService,
                clubAuthorizationService,
                clubMemberRepository,
                fixedClock);

        creator = User.builder()
                .id(42L)
                .build();
    }

    // ---------- createClub tests ----------

    @Test
    void createClub_shouldCreateAndReturnClubMappedToClubDto_whenClubRequestIsValidAndImageIsValidAndReported() {
    	MultipartFile mockImage = mock(MultipartFile.class);
    	CreateClubRequest request = new CreateClubRequest(
    			"test club",
    			"desc",
                true,
    			mockImage
    			);

    	Club club = Club.builder()
    			.id(1L)
    			.name(request.name())
    			.description(request.description())
                .createdAt(fixedTime)
    			.build();

    	ClubMember owner = ClubMember.builder()
    			.id(new ClubMemberId(club.getId(), creator.getId()))
    			.user(creator)
    			.club(club)
    			.clubRole(ClubRole.OWNER)
    			.status(JoinStatus.APPROVED)
    			.joinedAt(fixedTime)
    			.build();
    	String imageUrl = "imageUrl";
    
    	ClubDto expected = ClubDto.builder()
    			.id(club.getId())
    			.name(request.name())
    			.description(request.description())
    			.imageUrl(imageUrl)
    			.createdAt(fixedTime) // fixated
    			.build();

        when(clubRepository.save(any(Club.class))).thenReturn(club);
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(owner);
        when(firebaseStorageService.uploadClubImage(club.getId(), mockImage)).thenReturn(imageUrl);
        when(clubMapper.toDto(club)).thenReturn(expected);

        ClubDto actual = clubService.createClub(request, creator);

        assertNotNull(actual);
        assertEquals(expected, actual);

        verify(clubRepository, times(1)).save(any(Club.class));

        ArgumentCaptor<ClubMember> memberCaptor = ArgumentCaptor.forClass(ClubMember.class);

        verify(clubMemberRepository, times(1)).save(memberCaptor.capture());
        ClubMember capturedMember = memberCaptor.getValue();
        assertEquals(ClubRole.OWNER, capturedMember.getClubRole());
        assertEquals(creator.getId(), capturedMember.getUser().getId());

        verify(firebaseStorageService, times(1)).uploadClubImage(club.getId(), mockImage);
        verifyNoMoreInteractions(clubRepository, clubMemberRepository, firebaseStorageService, clubMapper);
    }

    @Test
    void createClub_shouldCreateAndReturnClubMappedToClubDto_whenClubRequestIsValidAndImageIsNullAndNotReported() {
        CreateClubRequest request = new CreateClubRequest(
                "test club",
                "desc",
                false,
                null // image
        );

        Club club = Club.builder()
                .id(1L)
                .name(request.name())
                .description(request.description())
                .createdAt(fixedTime)
                .build();

        ClubMember owner = ClubMember.builder()
                .id(new ClubMemberId(club.getId(), creator.getId()))
                .user(creator)
                .club(club)
                .clubRole(ClubRole.OWNER)
                .status(JoinStatus.APPROVED)
                .joinedAt(fixedTime)
                .build();

        ClubDto expected = ClubDto.builder()
                .id(club.getId())
                .name(request.name())
                .description(request.description())
                .createdAt(fixedTime) // fixated
                .build();

        when(clubRepository.save(any(Club.class))).thenReturn(club);
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(owner);
        when(clubMapper.toDto(club)).thenReturn(expected);

        ClubDto actual = clubService.createClub(request, creator);

        assertNotNull(actual);
        assertEquals(expected, actual);

        verify(clubRepository, times(1)).save(any(Club.class));

        ArgumentCaptor<ClubMember> memberCaptor = ArgumentCaptor.forClass(ClubMember.class);

        verify(clubMemberRepository, times(1)).save(memberCaptor.capture());
        ClubMember capturedMember = memberCaptor.getValue();
        assertEquals(ClubRole.OWNER, capturedMember.getClubRole());
        assertEquals(creator.getId(), capturedMember.getUser().getId());

        verify(firebaseStorageService, never())
                .uploadClubImage(anyLong(), isNull());
    }

    // ---------- deleteClub tests ----------

    @Test
    void deleteClubImage_shouldDeleteImageAndUpdateClub_whenClubExistsAndUserIsAuthorized() {
        Long clubId = 1L;
        Long requesterId = 10L;
        String existingImageUrl = "http://firebase.storage/path/to/image.jpg";

        User requester = User.builder().id(requesterId).build();

        Club mockClub = Club.builder()
                .id(clubId)
                .imageUrl(existingImageUrl)
                .build();

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(mockClub));

        doNothing().when(clubAuthorizationService).authorize(clubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);
        doNothing().when(firebaseStorageService).deleteClubImage(existingImageUrl);

        clubService.deleteClubImage(clubId, requester);

        verify(clubAuthorizationService, times(1)).authorize(clubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);

        verify(firebaseStorageService, times(1)).deleteClubImage(existingImageUrl);

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(clubRepository, times(1)).save(clubCaptor.capture());

        Club savedClub = clubCaptor.getValue();
        assertNull(savedClub.getImageUrl(), "The club's image URL should be null after deletion.");
        assertEquals(clubId, savedClub.getId(), "The ID of the saved club should not change.");
    }

    @Test
    void deleteClubImage_shouldThrowClubNotFoundException_whenClubDoesNotExist() {
        Long nonExistentClubId = 99L;
        User requester = User.builder().id(10L).build();

        when(clubRepository.findById(nonExistentClubId)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> {
            clubService.deleteClubImage(nonExistentClubId, requester);
        });

        verify(clubAuthorizationService, never()).authorize(anyLong(), anyLong(), Privilege.EDIT_CLUB_SETTINGS);
        verify(firebaseStorageService, never()).deleteClubImage(anyString());
        verify(clubRepository, never()).save(any(Club.class));
    }

    // ---------- searchClubs tests ----------

    @Test
    void searchClubs_shouldReturnPagedResponseOfClubDtos_whenClubsAreFound() {
        String query = "community";
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/clubs/search");
        request.setQueryString("query=community&page=0&size=2");

        Club club1 = Club.builder().id(1L).name("Tech Community").description("A club for tech lovers.").build();
        Club club2 = Club.builder().id(2L).name("Book Readers").description("A community for bookworms.").build();
        List<Club> foundClubs = Arrays.asList(club1, club2);

        Page<Club> clubPage = new PageImpl<>(foundClubs, pageable, 5);

        ClubDto dto1 = new ClubDto(1L, "Tech Community", "A club for tech lovers.", null, LocalDateTime.now());
        ClubDto dto2 = new ClubDto(2L, "Book Readers", "A community for bookworms.", null, LocalDateTime.now());
        List<ClubDto> clubDtos = Arrays.asList(dto1, dto2);

        when(clubRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable))
                .thenReturn(clubPage);
        when(clubMapper.toDtoList(foundClubs)).thenReturn(clubDtos);

        PagedResponse<ClubDto> actualResponse = clubService.searchClubs(query, pageable, request);

        assertNotNull(actualResponse);
        assertEquals(2, actualResponse.getItems().size(), "Should return the correct number of items on the page.");
        assertEquals(clubDtos, actualResponse.getItems(), "The items in the response should match the mapped DTOs.");

        assertEquals(page, actualResponse.getPage());
        assertEquals(size, actualResponse.getSize());
        assertEquals(5, actualResponse.getTotal(), "Total elements should match the mock page's total.");
        assertEquals(3, actualResponse.getTotalPages(), "Total pages should be calculated correctly (5 total / 2 size = 3 pages).");
        assertTrue(actualResponse.isHasNext(), "Should have a next page.");
        assertFalse(actualResponse.isHasPrevious(), "Should not have a previous page as we are on page 0.");

        assertTrue(actualResponse.getHref().endsWith("/clubs/search?query=community&page=0&size=2"), "Current page URL should be correct.");
        assertTrue(actualResponse.getNext().endsWith("/clubs/search?query=community&page=1&size=2"), "Next page URL should be correct.");
        assertNull(actualResponse.getPrevious(), "Previous page URL should be null.");

        verify(clubRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(eq(query), eq(query), eq(pageable));
        verify(clubMapper, times(1)).toDtoList(eq(foundClubs));
    }

    @Test
    void searchClubs_shouldReturnEmptyPagedResponse_whenNoClubsAreFound() {
        String query = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/clubs/search");

        Page<Club> emptyClubPage = Page.empty(pageable);
        when(clubRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable))
                .thenReturn(emptyClubPage);

        when(clubMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        PagedResponse<ClubDto> actualResponse = clubService.searchClubs(query, pageable, request);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.getItems().isEmpty(), "The items list should be empty.");
        assertEquals(0, actualResponse.getPage());
        assertEquals(10, actualResponse.getSize());
        assertEquals(0, actualResponse.getTotal(), "Total elements should be zero.");
        assertEquals(0, actualResponse.getTotalPages(), "Total pages should be zero.");
        assertFalse(actualResponse.isHasNext(), "Should not have a next page.");
        assertFalse(actualResponse.isHasPrevious(), "Should not have a previous page.");
        assertNull(actualResponse.getNext(), "Next page URL should be null.");
        assertNull(actualResponse.getPrevious(), "Previous page URL should be null.");

        verify(clubRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);
        verify(clubMapper, times(1)).toDtoList(Collections.emptyList());
    }

    // ---------- togglePrivacy tests ----------

    @Test
    void togglePrivacy_shouldTogglePrivacyState_whenClubExistsAndUserIsAuthorized() {
        Long clubId = 1L;
        User requester = User.builder().id(10L).username("testuser").build();

        Club mockClub = Club.builder()
                .id(clubId)
                .name("Test Club")
                .isPrivate(false)
                .build();

        doNothing().when(clubAuthorizationService).authorize(clubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(mockClub));

        clubService.togglePrivacy(clubId, requester);

        verify(clubAuthorizationService, times(1)).authorize(clubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(clubRepository, times(1)).save(clubCaptor.capture());

        Club savedClub = clubCaptor.getValue();
        assertTrue(savedClub.isPrivate(), "The club's privacy should be toggled to true (PRIVATE).");
        assertEquals(clubId, savedClub.getId(), "The club ID should remain unchanged.");
    }

    @Test
    void togglePrivacy_shouldThrowClubNotFoundException_whenClubDoesNotExist() {
        Long nonExistentClubId = 99L;
        User requester = User.builder().id(10L).build();

        doNothing().when(clubAuthorizationService).authorize(nonExistentClubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);
        when(clubRepository.findById(nonExistentClubId)).thenReturn(Optional.empty());

        assertThrows(ClubNotFoundException.class, () -> {
            clubService.togglePrivacy(nonExistentClubId, requester);
        });

        verify(clubAuthorizationService, times(1)).authorize(nonExistentClubId, requester.getId(), Privilege.EDIT_CLUB_SETTINGS);

        verify(clubRepository, never()).save(any(Club.class));
    }

}