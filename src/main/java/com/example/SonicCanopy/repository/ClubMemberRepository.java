package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {

    @Query("SELECT cm.club FROM ClubMember cm WHERE cm.user.id = :userId")
    List<Club> findClubsByUserId(Long userId);

    boolean existsByUserAndClub(User user, Club club);

    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);

    Optional<ClubMember> findByClubIdAndUserIdAndStatus(Long clubId, Long userId, JoinStatus  joinStatus);

    // In your ClubMemberRepository
    Page<ClubMember> findByClubIdAndStatus(Long clubId, JoinStatus status, Pageable pageable);


    Long club(Club club);
}