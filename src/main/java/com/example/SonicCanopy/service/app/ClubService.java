package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.mapper.ClubMapper;
import com.example.SonicCanopy.repository.ClubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClubService  {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper) {
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
    }

    public ClubDto createClub(CreateClubRequestDto dto, User creator) {
        Club club = clubMapper.toEntity(dto, creator);
        club = clubRepository.save(club);
        return clubMapper.toDto(club);
    }

    public Page<ClubDto> searchClubs(String query, Pageable pageable) {
        Page<Club> clubsPage = clubRepository.findByNameContainingIgnoreCase(query, pageable);
        return clubsPage.map(clubMapper::toDto);
    }

}
