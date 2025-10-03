package com.example.SonicCanopy.repository;
import com.example.SonicCanopy.domain.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByClubIdOrderByCreatedAtDesc(Long clubId, Pageable pageable);

    Optional<Event> findByIdAndClubId(Long eventId, Long clubId);



}
