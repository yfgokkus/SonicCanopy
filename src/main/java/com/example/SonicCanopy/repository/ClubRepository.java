package com.example.SonicCanopy.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.SonicCanopy.domain.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByName(String name);

    boolean existsByName(String name);

    Page<Club> findByMembersUserId(Long userId, Pageable pageable);

    Page<Club> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

}
