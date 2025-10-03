package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.club.ClubDto;
import com.example.SonicCanopy.domain.entity.Club;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClubMapper {

    public ClubDto toDto(Club club) {
        if (club == null) return null;

        return ClubDto.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .imageUrl(club.getImageUrl())
                .createdAt(club.getCreatedAt())
                .build();
    }

    public List<ClubDto> toDtoList(List<Club> members) {
        return members.stream()
                .map(this::toDto)
                .toList();
    }

}