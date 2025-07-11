package com.example.SonicCanopy.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "club_member_roles",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                    @JoinColumn(name = "club_id", referencedColumnName = "club_id")
            },
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<ClubRole> roles;

    private LocalDateTime joinedAt;
}
