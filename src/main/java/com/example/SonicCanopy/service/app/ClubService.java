package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.club.ClubSearchResultDto;
import com.example.SonicCanopy.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.exception.club.ClubNotFoundException;
import com.example.SonicCanopy.exception.club.ImageDeletionException;
import com.example.SonicCanopy.exception.club.UnauthorizedActionException;
import com.example.SonicCanopy.mapper.ClubMapper;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import com.example.SonicCanopy.repository.ClubRepository;
import com.example.SonicCanopy.service.firebase.FirebaseStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubService  {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubMapper clubMapper;
    private final FirebaseStorageService firebaseStorageService;

    public ClubService(ClubRepository clubRepository, ClubMemberRepository clubMemberRepository, ClubMapper clubMapper, FirebaseStorageService firebaseStorageService) {
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.clubMapper = clubMapper;
        this.firebaseStorageService = firebaseStorageService;
    }

    public ClubDto createClub(CreateClubRequestDto dto, User creator) {
        Club club = clubMapper.toEntity(dto, creator);
        club = clubRepository.save(club);

        MultipartFile image = dto.profilePicture();
        if (image != null && !image.isEmpty()) {
            String imageUrl = firebaseStorageService.uploadClubProfileImage(club.getId(), image);
            club.setPictureUrl(imageUrl);
            club = clubRepository.save(club);
        }

        return clubMapper.toDto(club);
    }

    public void deleteClub(Long clubId, User requester) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (!club.getCreatedBy().getId().equals(requester.getId())) {
            throw new UnauthorizedActionException("Not allowed to delete this club");
        }

        // Optional: delete associated data (events, comments, memberships, etc.)
        // commentRepository.deleteByClubId(clubId);
        // eventRepository.deleteByClubId(clubId);
        // membershipRepository.deleteByClubId(clubId);

        if (club.getPictureUrl() != null && !club.getPictureUrl().isBlank()) {
            firebaseStorageService.deleteClubImageByUrl(club.getPictureUrl());
        }

        clubRepository.delete(club);
    }

    public String updateProfilePicture(Long clubId, MultipartFile file, User requester) {
        Club club   = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (!club.getCreatedBy().getId().equals(requester.getId())) {
            throw new UnauthorizedActionException("Not allowed to update this club");
        }

        String imageUrl = firebaseStorageService.uploadClubProfileImage(clubId, file);
        club.setPictureUrl(imageUrl);

        return imageUrl;
    }

    public void deleteClubImage(Long clubId, User requester) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        if (!club.getCreatedBy().getId().equals(requester.getId())) {
            throw new UnauthorizedActionException("Not allowed to update this club");
        }

        if (club.getPictureUrl() != null && !club.getPictureUrl().isBlank()) {
            firebaseStorageService.deleteClubImageByUrl(club.getPictureUrl());
            club.setPictureUrl(null);
            clubRepository.save(club);
        }
    }

    //    public Page<ClubDto> searchClubs(String query, Pageable pageable) {
//        Page<Club> clubsPage = clubRepository.findByNameContainingIgnoreCase(query, pageable);
//        return clubsPage.map(clubMapper::toDto);
//    }
    public ClubSearchResultDto searchClubs(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Club> resultPage = clubRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable);

        List<ClubDto> clubDtos = resultPage.getContent()
                .stream()
                .map(clubMapper::toDto)
                .collect(Collectors.toList());

        return new ClubSearchResultDto(
                clubDtos,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalPages(),
                resultPage.getTotalElements(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    public List<ClubDto> getUserClubs(Long userId) {
        List<Club> clubs = clubMemberRepository.findClubsByUserId(userId);
        return clubs.stream().map(clubMapper::toDto).toList();
    }

}
