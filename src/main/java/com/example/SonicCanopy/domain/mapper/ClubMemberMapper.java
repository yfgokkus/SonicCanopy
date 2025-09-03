package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.clubMember.ClubMemberDto;
import com.example.SonicCanopy.domain.entity.ClubMember;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClubMemberMapper {
    public ClubMemberDto toDto(ClubMember member) {
        return new ClubMemberDto(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getClub().getId(),
                member.getClub().getName(),
                member.getClubRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }

    public List<ClubMemberDto> toDtoList(List<ClubMember> members) {
        return members.stream()
                .map(this::toDto)
                .toList(); // or .collect(Collectors.toList()) if Java < 16
    }
}