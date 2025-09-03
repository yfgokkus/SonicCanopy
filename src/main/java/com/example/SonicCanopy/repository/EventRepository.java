package com.example.SonicCanopy.repository;
import com.example.SonicCanopy.domain.entity.Club;
import com.example.SonicCanopy.domain.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByClubIdOrderByCreatedAtDesc(Long clubId, Pageable pageable);

    Optional<Event> findByIdAndClubId(Long eventId, Long clubId);

    @Query("SELECT c.isPrivate FROM Club c WHERE c.id = :clubId")
    boolean isPrivate(@Param("clubId") Long clubId);

}
