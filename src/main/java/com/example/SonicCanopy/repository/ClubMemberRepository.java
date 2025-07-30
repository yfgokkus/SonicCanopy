package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.ClubMember;
import com.example.SonicCanopy.entities.ClubMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {

    @Query("SELECT cm.club FROM ClubMember cm WHERE cm.user.id = :userId")
    List<Club> findClubsByUserId(Long userId);
}