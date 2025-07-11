package com.example.SonicCanopy.mapper;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.dto.club.CreateClubRequestDto;
import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.User;
import org.springframework.stereotype.Component;

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