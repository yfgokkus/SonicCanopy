package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.domain.entity.Club;
import com.example.SonicCanopy.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClubMapper {

    public ClubDto toDto(Club club) {
        if (club == null) return null;

        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getDescription(),
                club.getPictureUrl(),
                club.getCreatedAt(),
                club.getCreatedBy() != null ? club.getCreatedBy().getUsername() : null
        );
    }

    public List<ClubDto> toDtoList(List<Club> members) {
        return members.stream()
                .map(this::toDto)
                .toList();
    }

    public Club toEntity(CreateClubRequestDto dto, User creator) {
        if (dto == null) return null;

        Club club = new Club();
        club.setName(dto.name());
        club.setDescription(dto.description());
        club.setCreatedBy(creator);
        // createdAt will be set by @PrePersist
        return club;
    }
}