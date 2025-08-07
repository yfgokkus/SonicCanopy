package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {

    @Query("""
    SELECT cm.club FROM ClubMember cm
    WHERE cm.user.id = :userId
    """)
    Page<Club> findClubsByUserId(@Param("userId") Long userId, Pageable pageable);


    boolean existsByUserAndClub(User user, Club club);

    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);

    Optional<ClubMember> findByClubIdAndUserIdAndStatus(Long clubId, Long userId, JoinStatus  joinStatus);

    Page<ClubMember> findByClubIdAndStatus(Long clubId, JoinStatus status, Pageable pageable);

    @Query("""
    SELECT cm FROM ClubMember cm
    WHERE cm.club.id = :clubId
    ORDER BY
        CASE cm.clubRole
            WHEN 'OWNER' THEN 1
            WHEN 'ADMIN' THEN 2
            WHEN 'NERD' THEN 3
            WHEN 'MEMBER' THEN 4
            ELSE 5
        END,
        cm.joinedAt DESC
    """)
    Page<ClubMember> findAllByClubIdOrderedByRole(@Param("clubId") Long clubId, Pageable pageable);
}