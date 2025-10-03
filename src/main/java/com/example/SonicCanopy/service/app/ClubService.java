package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.club.CreateClubRequest;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.*;
import com.example.SonicCanopy.domain.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.domain.mapper.ClubMapper;
import com.example.SonicCanopy.domain.util.PaginationUtils;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.service.infrastructure.firebase.concretes.FirebaseStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ClubService  {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final FirebaseStorageService firebaseStorageService;
    private final ClubAuthorizationService clubAuthorizationService;
    private final ClubMemberRepository clubMemberRepository;

    private final Clock clock;

    public ClubService(ClubRepository clubRepository,
                       ClubMapper clubMapper,
                       FirebaseStorageService firebaseStorageService,
                       ClubAuthorizationService clubAuthorizationService,
                       ClubMemberRepository clubMemberRepository,
                       Clock clock) {
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
        this.firebaseStorageService = firebaseStorageService;
        this.clubAuthorizationService = clubAuthorizationService;
        this.clubMemberRepository = clubMemberRepository;
        this.clock = clock;
    }

    @Transactional
    public ClubDto createClub(CreateClubRequest dto, User creator) {
        Club club = Club.builder()
                .name(dto.name())
                .description(dto.description())
                .createdAt(LocalDateTime.now(clock))
                .build();

        club = clubRepository.save(club); //managed entity

        saveOwner(club,  creator);

        if(dto.imageProvided()){
            //storage service validates file
            String imageUrl = firebaseStorageService
                    .uploadClubImage(club.getId(), dto.image());
            club.setImageUrl(imageUrl);
        }

        return clubMapper.toDto(club);
    }

    @Transactional
    public void deleteClub(Long clubId, User requester) {
        Club club = getClub(clubId);

        clubAuthorizationService.authorizeClubDeletion(clubId, requester);

        firebaseStorageService.deleteClubImage(club.getImageUrl());

        clubRepository.delete(club);
    }

    @Transactional
    public String uploadClubImage(Long clubId, MultipartFile file, User requester) {
        Club club = getClub(clubId);

        clubAuthorizationService.authorizeClubSettingsManagement(clubId, requester);
        
        //upload new image before so that old one won't be deleted on failure
        String newImageUrl = firebaseStorageService.uploadClubImage(clubId, file); 

        //validates image url
        firebaseStorageService.deleteClubImage(club.getImageUrl());
        
        club.setImageUrl(newImageUrl);

        clubRepository.save(club);

        return newImageUrl;
    }

    @Transactional
    public void deleteClubImage(Long clubId, User requester) {
        Club club = getClub(clubId);

        clubAuthorizationService.authorizeClubSettingsManagement(clubId, requester);

        firebaseStorageService.deleteClubImage(club.getImageUrl());
        club.setImageUrl(null);
        clubRepository.save(club);
    }

    public PagedResponse<ClubDto> searchClubs(String query, Pageable pageable, HttpServletRequest request) {
        Page<Club> clubPage = clubRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);

        List<ClubDto> clubDtos = clubMapper.toDtoList(clubPage.getContent());

        return PaginationUtils.buildPagedResponse(clubDtos, clubPage, request);
    }

    public void togglePrivacy(Long clubId, User requester) {

        clubAuthorizationService.authorizeClubSettingsManagement(clubId, requester);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        club.setPrivate(!club.isPrivate());

        clubRepository.save(club);

        log.info("User {} toggled privacy for club {}. New state: {}",
                requester.getUsername(),
                club.getName(),
                club.isPrivate() ? "PRIVATE" : "PUBLIC");
    }
    
    private Club getClub(Long clubId) {
            return clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
    }

    private void saveOwner(Club club, User creator) {
        ClubMember owner = ClubMember.builder()
                .id(new ClubMemberId(club.getId(), creator.getId()))
                .user(creator)
                .club(club)
                .clubRole(ClubRole.OWNER)
                .status(JoinStatus.APPROVED)
                .joinedAt(LocalDateTime.now(clock))
                .build();
        clubMemberRepository.save(owner);
    }


}
