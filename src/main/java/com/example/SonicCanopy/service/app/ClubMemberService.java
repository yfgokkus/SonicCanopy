package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.dto.club.ClubDto;
import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.mapper.ClubMapper;
import com.example.SonicCanopy.repository.ClubMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubMemberService {
    private final ClubMapper clubMapper;
    private final ClubMemberRepository clubMemberRepository;

    public ClubMemberService(ClubMapper clubMapper, ClubMemberRepository clubMemberRepository) {
        this.clubMapper = clubMapper;
        this.clubMemberRepository = clubMemberRepository;
    }


}
