package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.entities.Club;
import com.example.SonicCanopy.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByClub(Club club, Pageable pageable);
    Page<Event> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
