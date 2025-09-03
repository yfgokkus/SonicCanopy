package com.example.SonicCanopy.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club_members")
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class ClubMember {

    @EmbeddedId
    private ClubMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clubId")
    @JoinColumn(name = "club_id")
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubRole clubRole;

    @Enumerated(EnumType.STRING)
    private JoinStatus status;

    private LocalDateTime joinedAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}
