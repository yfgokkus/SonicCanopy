package com.example.SonicCanopy.repository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.SonicCanopy.domain.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Page<Club> findByMembersUserId(Long userId, Pageable pageable);

    Page<Club> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    @Query("SELECT c.isPrivate FROM Club c WHERE c.id = :clubId")
    boolean isPrivate(@Param("clubId") Long clubId);
}
