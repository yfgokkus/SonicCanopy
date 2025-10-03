package com.example.SonicCanopy.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

    @Entity
    @Table(name = "events")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Event {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        private String description;

        @Column(nullable = false)
        private Long eventDurationMs;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "club_id", nullable = false)
        private Club club;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "created_by", nullable = false)
        private User createdBy;

        @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Comment> comments;

        @Column
        private String spotifyContentUri;

        @PrePersist
        public void prePersist() {
            this.createdAt = LocalDateTime.now();
        }
    }
